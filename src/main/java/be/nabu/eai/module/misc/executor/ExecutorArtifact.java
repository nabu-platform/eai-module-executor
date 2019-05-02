package be.nabu.eai.module.misc.executor;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.repository.RepositoryThreadFactory;
import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.artifacts.jaxb.JAXBArtifact;
import be.nabu.libs.artifacts.api.StartableArtifact;
import be.nabu.libs.artifacts.api.StoppableArtifact;
import be.nabu.libs.authentication.api.Token;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.services.ServiceRunnable;
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.ServiceUtils;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.services.api.ServiceResult;
import be.nabu.libs.services.api.ServiceRunnableObserver;
import be.nabu.libs.services.api.ServiceRunner;
import be.nabu.libs.types.api.ComplexContent;

public class ExecutorArtifact extends JAXBArtifact<ExecutorConfiguration> implements StartableArtifact, StoppableArtifact, ServiceRunner {

	private ExecutorService executors;
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public ExecutorArtifact(String id, ResourceContainer<?> directory, Repository repository) {
		super(id, directory, repository, "executor.xml", ExecutorConfiguration.class);
	}

	@Override
	public void stop() throws IOException {
		if (isStarted()) {
			executors.shutdown();
			try {
				executors.awaitTermination(365, TimeUnit.DAYS);
			}
			catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			finally {
				executors = null;
			}
		}
	}
	
	public ExecutorService getExecutorService() {
		return isStarted() ? executors : null;
	}

	@Override
	public void start() throws IOException {
		if (executors == null || executors.isShutdown()) {
			RepositoryThreadFactory threadFactory = new RepositoryThreadFactory(getRepository(), true);
			Integer poolSize = getConfig().getPoolSize();
			if (poolSize == null) {
				poolSize = Runtime.getRuntime().availableProcessors();
			}
			// if we explicitly set it to size 0, it is unlimited
			if (poolSize == 0) {
				executors = Executors.newCachedThreadPool(threadFactory);
			}
			else {
				executors = Executors.newFixedThreadPool(poolSize, threadFactory);
			}
		}
	}

	@Override
	public boolean isStarted() {
		return executors != null && !executors.isShutdown();
	}

	public Future<ServiceResult> run(Service service, ComplexContent input, Token token) {
		return executors.submit((Callable<ServiceResult>) new ServiceRunnable(new ServiceRuntime(service, getRepository().newExecutionContext(token)), input));
	}

	@Override
	public Future<ServiceResult> run(Service service, ExecutionContext context, ComplexContent input, ServiceRunnableObserver...observers) {
		ServiceRuntime runtime = new ServiceRuntime(service, context);
		// prevent reusing the thread local global, the service runtime will run in a different thread
		runtime.setContext(new HashMap<String, Object>());
		ServiceUtils.setServiceContext(runtime, ServiceUtils.getServiceContext(ServiceRuntime.getRuntime()));
		return executors.submit((Callable<ServiceResult>) new Callable<ServiceResult>() {
			@Override
			public ServiceResult call() throws Exception {
				try {
					return new ServiceRunnable(runtime, input, observers).call();
				}
				catch (Exception e) {
					logger.error("Could not run service" + (runtime.getService() instanceof DefinedService ? ": " + ((DefinedService) runtime.getService()).getId() : ""), e);
					throw e;
				}
			}
		});
	}
	
}

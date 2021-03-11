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
import be.nabu.libs.artifacts.api.OfflineableArtifact;
import be.nabu.libs.artifacts.api.StartableArtifact;
import be.nabu.libs.artifacts.api.StoppableArtifact;
import be.nabu.libs.artifacts.api.TwoPhaseStoppableArtifact;
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

public class ExecutorArtifact extends JAXBArtifact<ExecutorConfiguration> implements StartableArtifact, StoppableArtifact, ServiceRunner, OfflineableArtifact, TwoPhaseStoppableArtifact {

	private ExecutorService executors;
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public ExecutorArtifact(String id, ResourceContainer<?> directory, Repository repository) {
		super(id, directory, repository, "executor.xml", ExecutorConfiguration.class);
	}

	@Override
	public void stop() throws IOException {
		if (executors != null) {
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
		// @2020-08-18: added explicit setting of the service context, because if this is run by the "runInPool" (cause someone indicated $any), then there is no thread local for the service context
		// we "should" try and reuse the service context that originally triggered it but we currently don't have access to it
		// because we very rarely do this, it is currently more of a workaround than a full fix
		String serviceContext = ServiceUtils.getServiceContext(ServiceRuntime.getRuntime(), false);
		if (serviceContext == null) {
			if (service instanceof DefinedService) {
				serviceContext = ((DefinedService) service).getId();
			}
			else {
				throw new IllegalArgumentException("Could not run service because there is no service context");
			}
		}
		ServiceUtils.setServiceContext(runtime, serviceContext);
		if (getConfig().isDisableAuditing()) {
			runtime.getContext().put("audit.disabled", true);
		}
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

	/**
	 * We want the executor service to go "offline", in that we want the offlining process to wait until all queued tasks are done
	 * We then want to restart the executors, once everything is offline, there might still be a valid usecase to execute services on the executor
	 * So we immediately restart it for offline tasks
	 * 
	 * That means when we bring it back online, we don't actually have to do anything.
	 * It also means we want the executor to spin up when we are doing an offline start
	 */
	@Override
	public void online() throws IOException {
		// do nothing
	}

	@Override
	public void offline() throws IOException {
		stop();
		start();
	}

	@Override
	public void startOffline() throws IOException {
		start();
	}

	@Override
	public void halt() {
		if (executors != null) {
			executors.shutdown();
		}
	}
	
}

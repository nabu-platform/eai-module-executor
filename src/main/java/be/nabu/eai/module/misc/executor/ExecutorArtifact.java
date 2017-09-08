package be.nabu.eai.module.misc.executor;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import be.nabu.eai.repository.RepositoryThreadFactory;
import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.artifacts.jaxb.JAXBArtifact;
import be.nabu.libs.artifacts.api.StartableArtifact;
import be.nabu.libs.artifacts.api.StoppableArtifact;
import be.nabu.libs.resources.api.ResourceContainer;

public class ExecutorArtifact extends JAXBArtifact<ExecutorConfiguration> implements StartableArtifact, StoppableArtifact {

	private ExecutorService executors;
	
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
			executors = Executors.newFixedThreadPool(getConfig().getPoolSize(), threadFactory);
		}
	}

	@Override
	public boolean isStarted() {
		return executors != null && !executors.isShutdown();
	}

}

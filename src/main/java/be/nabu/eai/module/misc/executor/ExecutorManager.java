package be.nabu.eai.module.misc.executor;

import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.managers.base.JAXBArtifactManager;
import be.nabu.libs.resources.api.ResourceContainer;

public class ExecutorManager extends JAXBArtifactManager<ExecutorConfiguration, ExecutorArtifact> {

	public ExecutorManager() {
		super(ExecutorArtifact.class);
	}

	@Override
	protected ExecutorArtifact newInstance(String id, ResourceContainer<?> container, Repository repository) {
		return new ExecutorArtifact(id, container, repository);
	}

}

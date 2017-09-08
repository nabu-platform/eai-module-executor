package be.nabu.eai.module.misc.executor;

import java.io.IOException;
import java.util.List;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.base.BaseJAXBGUIManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;

public class ExecutorGUIManager extends BaseJAXBGUIManager<ExecutorConfiguration, ExecutorArtifact> {

	public ExecutorGUIManager() {
		super("Executor Service", ExecutorArtifact.class, new ExecutorManager(), ExecutorConfiguration.class);
	}

	@Override
	protected List<Property<?>> getCreateProperties() {
		return null;
	}

	@Override
	protected ExecutorArtifact newInstance(MainController controller, RepositoryEntry entry, Value<?>... values) throws IOException {
		return new ExecutorArtifact(entry.getId(), entry.getContainer(), entry.getRepository());
	}

	@Override
	public String getCategory() {
		return "Miscellaneous";
	}
}

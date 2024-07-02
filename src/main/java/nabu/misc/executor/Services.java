package nabu.misc.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.eai.module.misc.executor.ExecutorArtifact;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.libs.artifacts.api.Artifact;
import nabu.misc.executor.types.ExecutorStatistics;

@WebService
public class Services {
	
	@WebResult(name = "statistics")
	public ExecutorStatistics getStatistics(@WebParam(name = "executorId") String id) {
		if (id == null) {
			return null;
		}
		Artifact artifact = EAIResourceRepository.getInstance().resolve(id);
		if (!(artifact instanceof ExecutorArtifact)) {
			throw new IllegalArgumentException("Not a threadpool: " + id);
		}
		ExecutorService executorService = ((ExecutorArtifact) artifact).getExecutorService();
		if (executorService instanceof ThreadPoolExecutor) {
			ThreadPoolExecutor executor = (ThreadPoolExecutor) executorService;
			ExecutorStatistics statistics = new ExecutorStatistics();
			statistics.setActiveThreads(executor.getActiveCount());
			statistics.setCompletedTaskCount(executor.getCompletedTaskCount());
			statistics.setMaximumPoolSize(executor.getMaximumPoolSize());
			statistics.setPoolSize(executor.getPoolSize());
			statistics.setQueueSize(executor.getQueue().size());
			return statistics;
		}
		return null;
	}
}

/*
* Copyright (C) 2017 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

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

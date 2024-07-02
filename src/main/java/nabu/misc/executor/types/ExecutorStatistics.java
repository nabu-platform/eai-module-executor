package nabu.misc.executor.types;

public class ExecutorStatistics {
	private long taskCount, activeThreads, completedTaskCount, maximumPoolSize, poolSize, queueSize;

	public long getTaskCount() {
		return taskCount;
	}

	public void setTaskCount(long taskCount) {
		this.taskCount = taskCount;
	}

	public long getActiveThreads() {
		return activeThreads;
	}

	public void setActiveThreads(long activeThreads) {
		this.activeThreads = activeThreads;
	}

	public long getCompletedTaskCount() {
		return completedTaskCount;
	}

	public void setCompletedTaskCount(long completedTaskCount) {
		this.completedTaskCount = completedTaskCount;
	}

	public long getMaximumPoolSize() {
		return maximumPoolSize;
	}

	public void setMaximumPoolSize(long maximumPoolSize) {
		this.maximumPoolSize = maximumPoolSize;
	}

	public long getPoolSize() {
		return poolSize;
	}

	public void setPoolSize(long poolSize) {
		this.poolSize = poolSize;
	}

	public long getQueueSize() {
		return queueSize;
	}

	public void setQueueSize(long queueSize) {
		this.queueSize = queueSize;
	}
}

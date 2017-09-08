package be.nabu.eai.module.misc.executor;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "executor")
public class ExecutorConfiguration {
	
	private int poolSize = 1;

	public int getPoolSize() {
		return poolSize;
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}
	
}

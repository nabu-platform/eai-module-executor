package be.nabu.eai.module.misc.executor;

import javax.xml.bind.annotation.XmlRootElement;

import be.nabu.eai.api.EnvironmentSpecific;

@XmlRootElement(name = "executor")
public class ExecutorConfiguration {
	
	private Integer poolSize;

	@EnvironmentSpecific
	public Integer getPoolSize() {
		return poolSize;
	}

	public void setPoolSize(Integer poolSize) {
		this.poolSize = poolSize;
	}
	
}

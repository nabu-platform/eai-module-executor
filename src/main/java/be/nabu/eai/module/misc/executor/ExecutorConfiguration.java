package be.nabu.eai.module.misc.executor;

import javax.xml.bind.annotation.XmlRootElement;

import be.nabu.eai.api.EnvironmentSpecific;

@XmlRootElement(name = "executor")
public class ExecutorConfiguration {
	
	private Integer poolSize;
	private boolean disableAuditing;

	@EnvironmentSpecific
	public Integer getPoolSize() {
		return poolSize;
	}

	public void setPoolSize(Integer poolSize) {
		this.poolSize = poolSize;
	}

	// allows you to perform tasks that are not passed through an auditor
	public boolean isDisableAuditing() {
		return disableAuditing;
	}
	public void setDisableAuditing(boolean disableAuditing) {
		this.disableAuditing = disableAuditing;
	}
}

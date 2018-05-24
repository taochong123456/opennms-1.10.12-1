package org.opennms.netmgt.syslogd.analyze;

import java.util.List;

public class PortResource implements java.io.Serializable {
	/**
	 * 1 表示NOT，0表示OR，
	 */
	private int logicFlag = 0;

	/**
	 * 起始端口
	 */
	private long startPort = -1;
	/**
	 * 结束端口
	 */
	private long endPort = -1;

	/**
	 * 各段端口的列表和，如果segments为null表示startPort，endPort起作用；如果segments不为null,
	 * 表示segments起作用
	 */
	private List<PortResource> segments;

	public long getEndPort() {
		return endPort;
	}

	public void setEndPort(long endPort) {
		this.endPort = endPort;
	}

	public int getLogicFlag() {
		return logicFlag;
	}

	public void setLogicFlag(int logicFlag) {
		this.logicFlag = logicFlag;
	}

	public List<PortResource> getSegments() {
		return segments;
	}

	public void setSegments(List<PortResource> segments) {
		this.segments = segments;
	}

	public long getStartPort() {
		return startPort;
	}

	public void setStartPort(long startPort) {
		this.startPort = startPort;
	}

}

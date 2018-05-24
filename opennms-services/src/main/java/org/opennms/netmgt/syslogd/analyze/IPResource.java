package org.opennms.netmgt.syslogd.analyze;

import java.util.List;

public class IPResource implements java.io.Serializable {
	/**
	 * 1 表示NOT，0表示OR，
	 */
	private int logicFlag;

	/**
	 * 起始IP
	 */
	private String startIP;

	/**
	 * 结束IP
	 */
	private String endIP;

	/**
	 * 子网掩码的数字表示，从1到31.如果ipMask!=-1
	 * 表示startIP和ipMask起作用；如果ipMask==-1，表示startIP和endIP起作用
	 */
	private int ipMask = -1;

	/**
	 * 各段IP的列表和，如果segments为null表示（startIP，endIP）或（startIP,ipMask）起作用；
	 * 如果segments不为null,表示segments起作用
	 */
	private List<IPResource> segments;

	public String getEndIP() {
		return endIP;
	}

	public void setEndIP(String endIP) {
		this.endIP = endIP;
	}

	public int getIpMask() {
		return ipMask;
	}

	public void setIpMask(int ipMask) {
		this.ipMask = ipMask;
	}

	public int getLogicFlag() {
		return logicFlag;
	}

	public void setLogicFlag(int logicFlag) {
		this.logicFlag = logicFlag;
	}

	public List<IPResource> getSegments() {
		return segments;
	}

	public void setSegments(List<IPResource> segments) {
		this.segments = segments;
	}

	public String getStartIP() {
		return startIP;
	}

	public void setStartIP(String startIP) {
		this.startIP = startIP;
	}

	public long getInternEndIP() {
		return internEndIP;
	}

	public void setInternEndIP(long internEndIP) {
		this.internEndIP = internEndIP;
	}

	public long getInternStartIP() {
		return internStartIP;
	}

	public void setInternStartIP(long internStartIP) {
		this.internStartIP = internStartIP;
	}

	private long internStartIP = -1;// start ip的整数表示。内部使用。

	private long internEndIP = -1;// end ip的整数表示。内部使用。

}

package org.opennms.netmgt.syslogd.parse;

public class Data {
	private String data;

	private String sourceIp;

	public String getData() {
		return data;
	}

	public String getSourceIp() {
		return sourceIp;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setSourceIp(String sourceIp) {
		this.sourceIp = sourceIp;
	}

	@Override
	public String toString() {
		return "Data [data=" + data + ", sourceIp=" + sourceIp + "]";
	}
}

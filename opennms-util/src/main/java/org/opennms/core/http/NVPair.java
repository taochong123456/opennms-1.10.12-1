package org.opennms.core.http;

public class NVPair {
	String name = "";
	String value = "";

	public NVPair(NVPair data) {
		this.name = data.getName();
		this.value = data.getValue();
	}

	public NVPair(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return this.name;
	}

	public String getValue() {
		return this.value;
	}

	public String toString() {
		return this.name + ":" + this.value;
	}
}
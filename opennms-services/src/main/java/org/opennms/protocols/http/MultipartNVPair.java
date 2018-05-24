package org.opennms.protocols.http;

public class MultipartNVPair {
	String name = "";
	Object value = null;

	public MultipartNVPair(MultipartNVPair data) {
		this.name = data.getName();
		this.value = data.getValue();
	}

	public MultipartNVPair(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return this.name;
	}

	public Object getValue() {
		return this.value;
	}

	public String toString() {
		return this.name + ":" + this.value.toString();
	}
}
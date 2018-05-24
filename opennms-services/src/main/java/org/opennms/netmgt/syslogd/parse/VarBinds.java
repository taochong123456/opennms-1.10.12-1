package org.opennms.netmgt.syslogd.parse;

public class VarBinds implements java.io.Serializable {
	private String type;

	private String ref;

	private String attrname;

	private String[] values;

	private Object internValueObject;

	public void setValue(String value) {
		if (values == null) {
			values = new String[1];
		}
		values[0] = value;
	}

	public String getValue() {
		if (values == null || values.length == 0)
			return null;
		else
			return values[0];
	}

	public String getAttrname() {
		return attrname;
	}

	public void setAttrname(String attrname) {
		this.attrname = attrname;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String[] getValues() {
		return values;
	}

	public void setValues(String[] values) {
		this.values = values;
	}

	public Object getInternValueObject() {
		return internValueObject;
	}

	public void setInternValueObject(Object internValueObject) {
		this.internValueObject = internValueObject;
	}

}
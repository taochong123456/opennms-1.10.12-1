package org.opennms.netmgt.syslogd.parse;
import java.util.Map;
public class SIMEventParameter {

	private String definedID;

	private long receptTime;

	private Object eventParas;

	public SIMEventParameter() {

	}

	public SIMEventParameter(String definedID, long receptTime,
			Object eventParas) {
		this.definedID = definedID;
		this.receptTime = receptTime;
		this.eventParas = eventParas;
	}

	public void setValues(String definedID, long receptTime, Object eventParas) {
		this.definedID = definedID;
		this.receptTime = receptTime;
		this.eventParas = eventParas;
	}

	public void clear() {
		definedID = null;
		receptTime = 0;
		eventParas = null;
	}

	public String getDefinedID() {
		return definedID;
	}

	public void setDefinedID(String definedID) {
		this.definedID = definedID;
	}

	public Object getEventParas() {
		return eventParas;
	}

	public void setEventParas(Object eventParas) {
		this.eventParas = eventParas;
	}

	public long getReceptTime() {
		return receptTime;
	}

	public void setReceptTime(long receptTime) {
		this.receptTime = receptTime;
	}

	public long getEventID() {
		if (eventParas instanceof SIMEventObject) {
			return ((SIMEventObject) eventParas).getID();
		} else
			return (Long) ((Map) eventParas).get("id");
	}
}

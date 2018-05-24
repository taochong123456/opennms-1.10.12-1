package org.opennms.netmgt.syslogd.analyze;
import java.util.Map;
import org.opennms.netmgt.syslogd.parse.SIMEventObject;

public class SIMEventParameter {
	/**
	 * 在过滤器或规则中定义的filter id
	 */
	private String definedID;

	/**
	 * 事件接受事件，其值等于相应的SIMEventObject中的receptTime值
	 */
	private long receptTime;

	/**
	 * 类型可以为Map:由相应的SIMEventObject的属性名字[全小写]与值组成的键值对。 类型也可以为SimEventObject.
	 */
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

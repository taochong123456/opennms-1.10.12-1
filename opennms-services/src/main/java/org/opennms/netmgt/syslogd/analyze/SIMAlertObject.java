package org.opennms.netmgt.syslogd.analyze;
import org.opennms.netmgt.syslogd.parse.SIMEventObject;
public class SIMAlertObject extends SIMEventObject {
	/**
	 * 为t_alert表准备的告警id
	 */
	public long t_alertID;
	/**
	 * 触发成该告警对象的原始事件ID
	 */
	public long originEventID;

	/**
	 * 获得工单流程中需要使用的alertid
	 * 
	 * @return
	 */
	public long getAlertIDforTicket() {
		return t_alertID;
	}

	public long getOriginEventID() {
		return originEventID;
	}

	public void setOriginEventID(long originEventID) {
		this.originEventID = originEventID;
	}

	public long getT_alertID() {
		return t_alertID;
	}

	public void setT_alertID(long t_alertid) {
		t_alertID = t_alertid;
	}

}

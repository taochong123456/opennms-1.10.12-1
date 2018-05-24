package org.opennms.netmgt.syslogd.analyze;
import org.opennms.netmgt.syslogd.parse.SIMEventObject;
public class SIMEventProxy {
	/**
	 * 事件对像
	 */
	private SIMEventObject event;
	/**
	 * 事件所在缓冲区
	 */
	private Object buffer;
	/**
	 * 事件所在缓冲区中的位置。
	 */
	private int position;

	public Object getBuffer() {
		return buffer;
	}

	public void setBuffer(Object buffer) {
		this.buffer = buffer;
	}

	public SIMEventObject getEvent() {
		return event;
	}

	public void setEvent(SIMEventObject event) {
		this.event = event;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
}

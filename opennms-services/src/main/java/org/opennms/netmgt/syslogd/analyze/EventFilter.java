package org.opennms.netmgt.syslogd.analyze;

import org.opennms.netmgt.syslogd.parse.SIMEventObject;

public abstract class EventFilter implements EventAction {
	/**
	 * 返回结果符合处理条件
	 */
	public static final int INCLUDE = 1;

	/**
	 * 返回结果不符合处理条件
	 */
	public static final int EXCLUDE = -1;

	/**
	 * 处理事件
	 * 
	 * @param event
	 * @return 返回处理结果，对应INCLUDE和EXCLUDE常量
	 */
	public abstract int handle(SIMEventObject event);
}

package org.opennms.netmgt.syslogd.analyze;

import java.util.ArrayList;

import org.opennms.netmgt.syslogd.parse.EventFormater;

public abstract class EventHandleFactory {

	/**
	 * 创建事件归一化工具类
	 * 
	 * @return
	 */
	public abstract EventFormater createEventFormater();

	/**
	 * 创建事件处理类列表
	 * 
	 * @return
	 */
	public abstract ArrayList<EventAction> createEventAction();

}

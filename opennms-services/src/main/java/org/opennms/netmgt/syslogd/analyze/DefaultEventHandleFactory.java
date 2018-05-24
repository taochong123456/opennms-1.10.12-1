package org.opennms.netmgt.syslogd.analyze;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.opennms.netmgt.syslogd.parse.DefaultEventFormater;
import org.opennms.netmgt.syslogd.parse.EventFormater;

public class DefaultEventHandleFactory extends EventHandleFactory {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private ArrayList<EventAction> eventactions = new ArrayList<EventAction>();

	public DefaultEventHandleFactory() {
		init();
	}

	/**
	 * 初始化事件处理的配置
	 *
	 */
	private void init() {
		eventactions.add(new EventFilterAction("SIMEventFilter"));
	}

	/**
	 * 创建事件归一化工具类
	 * 
	 * @return
	 */
	public EventFormater createEventFormater() {
		return new DefaultEventFormater();
	}

	/**
	 * 创建定制事件处理类列表
	 * 
	 * @return
	 */
	public ArrayList<EventAction> createEventAction() {
		return eventactions;
	}

}

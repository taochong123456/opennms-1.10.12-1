package org.opennms.netmgt.syslogd.analyze;

import org.opennms.netmgt.syslogd.parse.SIMEventObject;

public class EventFilterAction extends EventFilter {
	private FilterDelegate filter = null;
	private String filtertype = "SIMEventFilter";

	public EventFilterAction(String filtertype) {
		this.filtertype = filtertype;
	}

	/**
	 * 存储事件
	 */
	public int handle(SIMEventObject event) {
		if (filter.isSatisfied(event))
			return this.INCLUDE;
		else
			return this.EXCLUDE;
	}

	/**
	 * 获取Action名称
	 * 
	 * @return
	 */
	public String getName() {
		return this.getClass().getName();
	}

	/**
	 * 设置名称
	 * 
	 * @param name
	 * @return
	 */
	public void setName(String name) {

	}

	/**
	 * 初始化
	 * 
	 * @return
	 */
	public boolean init() {
		EventServiceConfig config = EventServiceConfig.getEventServiceConfig();
		String id = config.getConfig(filtertype);
		if (id == null || id.equals(""))
			return false;

		filter = FilterManagerFactory.getFilterManager().getFilterDelegate(id);
		if (filter == null)
			return false;
		else
			return true;
	}

	/**
	 * 释放资源
	 * 
	 * @return
	 */
	public void free() {
	}

}

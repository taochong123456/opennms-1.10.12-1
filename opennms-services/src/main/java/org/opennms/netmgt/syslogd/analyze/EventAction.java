package org.opennms.netmgt.syslogd.analyze;

import org.opennms.netmgt.syslogd.parse.SIMEventObject;

public interface EventAction {
	/**
	 * 处理事件
	 * 
	 * @param event
	 */
	public int handle(SIMEventObject event);

	/**
	 * 获取Action名称
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * 设置名称
	 * 
	 * @param name
	 * @return
	 */
	public void setName(String name);

	/**
	 * 初始化
	 * 
	 * @return
	 */
	public boolean init();

	/**
	 * 释放资源
	 * 
	 * @return
	 */
	public void free();
}

package org.opennms.netmgt.syslogd.analyze;

import org.apache.log4j.Logger;
import org.opennms.netmgt.syslogd.parse.SIMEventObject;

public class RuleEnginAction implements EventAction {
	private Logger logger = Logger.getLogger(this.getClass().getName());
	private String name = null;

	/**
	 * 处理事件
	 * 
	 * @param event
	 */
	public int handle(SIMEventObject event) {
		// -- 向缓冲区添加收到的事件对像，以便实时显示数据和为规则判断提供基础数据。
		try {
			RuleEngineServiceHelper.addEvent(event);
		} catch (Exception e) {
			return 0;
		}

		return 1;
	}

	/**
	 * 获取Action名称
	 * 
	 * @return
	 */
	public String getName() {
		if (name == null)
			return "RuleEnginAction";
		return name;
	}

	/**
	 * 设置名称
	 * 
	 * @param name
	 * @return
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 初始化
	 * 
	 * @return
	 */
	public boolean init() {
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

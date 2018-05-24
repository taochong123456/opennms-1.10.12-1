package org.opennms.netmgt.syslogd.analyze;

import java.util.List;
import java.util.Map;

import org.opennms.netmgt.syslogd.parse.SIMEventObject;

public interface RuleRobot {

	/**
	 * 获取规则对像。
	 * 
	 * @return
	 */
	public SIMRuleObject getRuleObject();

	/**
	 * 判断给定事件是否满足规则要求
	 * 
	 * @param event
	 * @return
	 */
	public boolean isSatisfied(SIMEventObject event);

	/**
	 * 判断给定条件是否满足规则要求
	 * 
	 * @param varbinds
	 *            变量绑定，其中包含事件属性或其他属性
	 * @return
	 */
	public boolean isSatisfied(Map varbinds);

	/**
	 * 释放规则使用的资源
	 * 
	 */
	public void release();

	/**
	 * 获得满足当前规则的事件轨迹。
	 * 
	 * @return
	 */
	public List<List<Long>> getEventTrails();

}

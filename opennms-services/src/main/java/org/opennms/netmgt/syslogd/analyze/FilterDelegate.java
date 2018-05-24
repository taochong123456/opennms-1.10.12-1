package org.opennms.netmgt.syslogd.analyze;

import java.util.List;
import java.util.Map;

import org.opennms.netmgt.syslogd.parse.SIMEventObject;

public interface FilterDelegate {
	/**
	 * 获取过滤器对像。
	 * 
	 * @return
	 */
	public SIMFilterObject getFilterObject();

	/**
	 * 判断给定事件是否满足过滤器要求
	 * 
	 * @param event
	 * @return 调用函数需要判断是否产生异常
	 */
	public boolean isSatisfied(SIMEventObject event);

	/**
	 * 判断给定条件是否满足过滤器要求
	 * 
	 * @param varMap
	 *            由变量与对应的值组成的map，这里的变量包含事件属性或其他属性
	 * @return 调用函数需要判断是否产生异常
	 */
	public boolean isSatisfied(Map varMap);

	/**
	 * 判断给定条件是否满足过滤器要求
	 * 
	 * @param event
	 *            目前至少支持两种类型SIMEventObject和Map
	 * @return
	 */
	public boolean isSatisfied(Object event);

	/**
	 * 判断给定条件是否满足过滤器要求
	 * 
	 * @param events
	 *            由SIMEventParameter组成的参数列表。主要用于不同事件关联分析时使用。
	 * @return 调用函数需要判断是否产生异常
	 */
	public boolean isSatisfied(List<SIMEventParameter> events);

}

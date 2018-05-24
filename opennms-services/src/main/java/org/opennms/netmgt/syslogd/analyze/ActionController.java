package org.opennms.netmgt.syslogd.analyze;

import java.util.List;

public interface ActionController {
	/**
	 * 注册一种类型的动作执行器。
	 * 
	 * @param type
	 *            类型名称。
	 * @param actionExecutorClassName
	 *            动作执行器类名。
	 */
	void registerExecutor(String type, String actionExecutorClassName);

	/**
	 * 初始化告警动作控制分发器。可以重入。
	 * 
	 */
	void init();

	/**
	 * 关闭告警控制分发器。
	 */
	void shutdown();

	/**
	 * 判断告警控制分发器是否出于使能的状态。
	 * 
	 * @return 是否使能的工作状态。
	 */
	boolean isEnable();

	/**
	 * 设置告警控制分发器的使能状态。
	 * 
	 * @param enable
	 */
	void setEnable(boolean enable);

	/**
	 * 派发告警动作。
	 * 
	 * @param action
	 * @param evnp
	 */
	void dispatch(MActionObject action, String[][] evnp);

	/**
	 * 向动作对列中添加动作任务。
	 * 
	 * @param actions
	 * @param envp
	 */
	void addActionTask(List<MActionObject> actions, String[][] envp);

}

package org.opennms.netmgt.syslogd.analyze;

public interface RuleEngineService {
	/**
	 * 获得事件缓存管理器。
	 * 
	 * @return
	 */
	public EventBufferManager getEventBufferManager();

	/**
	 * 获得规则管理器。
	 * 
	 * @return
	 */
	public RuleManager getRuleManager();

	/**
	 * 获得过滤器管理器。
	 * 
	 * @return
	 */
	public FilterManager getFilterManager();

	/**
	 * 获得动作执行管理器。
	 * 
	 * @return
	 */
	public ActionController getActionController();

}

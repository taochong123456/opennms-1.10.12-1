package org.opennms.netmgt.syslogd.analyze;

public class FilterManagerFactory {
	/**
	 * 单例。
	 */
	private static FilterManager filterManager = new FilterManagerImpl();

	/**
	 * 获得过滤器管理器。
	 * 
	 * @return
	 */
	public static FilterManager getFilterManager() {
		return filterManager;
	}
}

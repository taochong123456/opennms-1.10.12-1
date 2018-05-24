package org.opennms.netmgt.syslogd.analyze;

public interface FilterManager {
	/**
	 * 初始化
	 * 
	 */
	public void init();

	/**
	 * 释放资源
	 * 
	 */
	public void release();

	/**
	 * 根据过滤器ID获取相应的过滤器对象。
	 * 
	 * @param filterID
	 * @return
	 */
	public SIMFilterObject getFilterByID(String filterID);

	/**
	 * 添加过滤器
	 * 
	 * @param filter
	 *            过滤器对像
	 * @return
	 */
	public boolean addFilter(SIMFilterObject filter);

	/**
	 * 更新过滤器
	 * 
	 * @param filter
	 *            过滤器对像
	 * @return
	 */
	public boolean updateFilter(SIMFilterObject filter);

	/**
	 * 删除过滤器
	 * 
	 * @param filterID
	 *            过滤器标识
	 * @return
	 */
	public boolean removeFilter(String filterID);

	/**
	 * 根据filterID获得指定过滤器对像的代理对像
	 * 
	 * @param filterID
	 * @return
	 */
	public FilterDelegate getFilterDelegate(String filterID);

	/**
	 * 访问临时过滤器。主要用于表示访问了临时过滤器。临时过滤器只存在于服务器内存中，如果在指定间隔间内没有被事件缓冲区访问过，将被删除。
	 * 
	 * @param filterID
	 */
	public void accessFilter(String filterID);

	/**
	 * 设置临时过滤器最长访问间隔。
	 * 
	 * @param seconds
	 *            单位是秒。
	 */
	public void setFilterAccessTimeOut(long seconds);
}

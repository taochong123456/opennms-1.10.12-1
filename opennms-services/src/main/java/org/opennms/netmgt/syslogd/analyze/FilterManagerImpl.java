package org.opennms.netmgt.syslogd.analyze;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
public class FilterManagerImpl implements FilterManager {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * 加载到内存中的全部过滤器。
	 */
	private Map<String, FilterDelegate> memoryFilters = new HashMap<String, FilterDelegate>();

	/**
	 * 临时过滤器访问记录。Map的元素表示<临时过滤器id，最近访问时间>。
	 */
	private Map<String, Long> tempFilterAccessMap = Collections
			.synchronizedMap(new HashMap<String, Long>());
	/**
	 * 临时过滤器访问超时时间，缺省1个小时
	 */
	private long tempFilterTimeOut = 1 * 60 * 60 * 1000; // 1个小时，单位ms
	/**
	 * 临时过滤器监控线程
	 */
	private FilterTimeOutMonitorThead filterTimeOutMonitorThread;

	/**
	 * 初始化
	 * 
	 */
	public void init() {
		// 加载过滤器
		try {
			logger.info("Prepare to initialize filters.");
			List list = RuleEngineServiceHelper.loadFiltersFromDB();
			if (list != null && list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					String filterName = (String) ((List) list.get(i)).get(0);
					String filterXMLStr = (String) ((List) list.get(i)).get(1);

					if (!StringTools.hasContent(filterXMLStr)) {
						logger.error("filter "
								+ filterName
								+ " doesn't has content. Can not be initialized.");
						continue;
					}

					try {
						SIMFilterObject filter = TransformHelper
								.transFromXMLToFilterObject(filterXMLStr);
						addFilter(filter);
						logger.info("init filter " + filter.getId() + " : "
								+ filter.getName() + " over");
						// secfox.soc.manage.audit.rule.support.TestingEnvironment.printFilterObjectContent(filter);
					} catch (Exception e) {
						logger.error("filter " + filterName
								+ " initialized failure.", e);
					}

				}
				logger.info("initialize filters over.");
			} else {
				logger.info("No filter needs to be initialized.");
			}
		} catch (Exception e) {
			logger.error("init filter error", e);
		}
	}

	/**
	 * 释放资源
	 * 
	 */
	public void release() {
		memoryFilters.clear();
		if (filterTimeOutMonitorThread != null) {
			filterTimeOutMonitorThread.setRunning(false);
			filterTimeOutMonitorThread.interrupt();
		}
	}

	public SIMFilterObject getFilterByID(String filterID) {
		FilterDelegate delegate = memoryFilters.get(filterID);
		if (delegate != null)
			return delegate.getFilterObject();
		else
			return null;
	}

	public boolean addFilter(SIMFilterObject filter) {
		if (filter == null || filter.getId() == null)
			return false;

		FilterDelegate oldDelegate = memoryFilters.get(filter.getId());
		if (oldDelegate != null)// 已经存在，不能增加为新的过滤器。
			return false;

		FilterDelegate delegate = new FilterDelegateImpl(filter);
		memoryFilters.put(filter.getId(), delegate);
		return true;
	}

	public boolean updateFilter(SIMFilterObject filter) {
		if (filter == null || filter.getId() == null)
			return false;

		FilterDelegate oldDelegate = memoryFilters.get(filter.getId());
		if (oldDelegate == null)// 过滤器不存在，不能更新。
			return false;

		FilterDelegate delegate = new FilterDelegateImpl(filter);
		memoryFilters.put(filter.getId(), delegate);
		return true;
	}

	public boolean removeFilter(String filterID) {
		FilterDelegate delegate = memoryFilters.remove(filterID);
		if (delegate != null)
			return true;
		else
			return false;// 删除的过滤器不存在。
	}

	public FilterDelegate getFilterDelegate(String filterID) {
		return memoryFilters.get(filterID);
	}

	public void accessFilter(String filterID) {
		if (filterID != null && filterID.trim().length() != 0)
			tempFilterAccessMap.put(filterID, System.currentTimeMillis());
	}

	public void setFilterAccessTimeOut(long seconds) {
		tempFilterTimeOut = Math.abs(seconds) * 1000;
	}

	/**
	 * 过滤器超时监控线程
	 * 
	 * @author zhuzhen
	 * 
	 */
	class FilterTimeOutMonitorThead extends Thread {
		boolean isRunning = true;

		public void setRunning(boolean isRunning) {
			this.isRunning = isRunning;
		}

		public void run() {
			while (isRunning && !isInterrupted()) {
				try {
					sleep(tempFilterTimeOut);
				} catch (Exception e) {
					// TODO: handle exception
				}
				try {
					Set theKS = tempFilterAccessMap.keySet();
					synchronized (tempFilterAccessMap) {
						Iterator iter = theKS.iterator();
						while (iter.hasNext()) {
							Object key = iter.next();
							Long lastAccessTime = (Long) tempFilterAccessMap
									.get(key);
							if ((System.currentTimeMillis() - lastAccessTime) > tempFilterTimeOut) { // 如果控制台session超时则删除session信息
								tempFilterAccessMap.remove(key);
							}
						}
					}
				} catch (Exception e) {
					logger.error("filter access timeout monitor error, ignore",
							e);
				}
			}
		}
	}
}

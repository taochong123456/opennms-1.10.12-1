package org.opennms.netmgt.syslogd.analyze;

import java.util.List;

import org.opennms.netmgt.syslogd.parse.SIMEventObject;

public interface EventBufferManager {
	/**
	 * 向缓冲区中，添加归一化后的事件对像。
	 * 
	 * @param event
	 *            需要向缓冲区中添加的归一化后的事件对象。
	 * @return 添加成功与否的标志。
	 */
	public boolean addEvent(SIMEventObject event);

	/**
	 * 获取事件对像列表。
	 * 
	 * @param startTime
	 *            事件接收时间，对应查询时间的起始点。
	 * @param endTime
	 *            事件接收时间，对应查询时间的结束点。
	 * @return 事件列表。队列中List[0]是最晚发生的事件
	 */
	public List getEvents(long startTime, long endTime);

	/**
	 * 获取事件对像列表。
	 * 
	 * @param startTime
	 *            事件接收时间，对应查询时间的起始点。
	 * @param endTime
	 *            事件接收时间，对应查询时间的结束点。
	 * @param filterID
	 *            应用于事件的过滤器id,如果系统中不存在指定的过滤器，则取回时间段内的全部事件对像.如果不存在过滤器，置为null.
	 * @Param maxEventCount
	 *        列表中返回的最大事件对像数目，这些事件都从endTime端起计数。如果maxEventCount<0,则不限制取回的事件个数。
	 * @return 事件列表。
	 */
	public List getEvents(long startTime, long endTime, String filterID,
			int maxEventCount);

	/**
	 * 获取事件对像列表。
	 * 
	 * @param startTime
	 *            事件接收时间，对应查询时间的起始点。
	 * @param filterID
	 *            应用于事件的过滤器id,如果系统中不存在指定的过滤器，则取回时间段内的全部事件对像。如果不存在过滤器，置为null.
	 * @param eventID
	 *            当前事件的ID,返回列表中不包括本事件。从当前eventID后面的事件开始获取。
	 *            该事件的接收时间就是startTime参数的值。如果eventID取值<0，则忽略
	 * @Param maxEventCount
	 *        列表中返回的事件对像数目。如果maxEventCount<0,则取回到当前时间为止的全部事件。这些事件都从endTime端起计数。
	 * @return 事件列表。
	 */
	public List getEvents(long startTime, String filterID, long eventID,
			int maxEventCount);

	/**
	 * 获取事件对像列表。
	 * 
	 * @param startTime
	 *            事件接收时间，对应查询时间的起始点。
	 * @param endTime
	 *            事件接收时间，对应查询时间的结束点。
	 * @param filterID
	 *            应用于事件的过滤器id,如果系统中不存在指定的过滤器，则取回时间段内的全部事件对像。如果不存在过滤器，置为null.
	 * @param eventID
	 *            当前事件的ID,返回列表中不包括本事件。从当前eventID后面发生的事件开始获取。
	 *            该事件的接收时间就是startTime参数的值。如果取值<0，则忽略
	 * @Param maxEventCount
	 *        列表中返回的最大事件对像数目。如果maxEventCount<0,则不限制取回的事件个数。这些事件都从endTime端起计数。
	 * @return 事件列表。
	 */
	public List getEvents(long startTime, long endTime, String filterID,
			long eventID, int maxEventCount);

	/**
	 * 计数并获取事件对像列表。
	 * 
	 * @param startTime
	 *            事件接收时间，对应查询时间的起始点。
	 * @param endTime
	 *            事件接收时间，对应查询时间的结束点。
	 * @param filterID
	 *            应用于事件的过滤器id,如果系统中不存在指定的过滤器，则取回时间段内的全部事件对像。如果不存在过滤器，
	 *            置为null置为null或空字符串.
	 * @param startEventID
	 *            开始事件的ID,返回列表中不包括本事件。从startEventID后面的事件开始获取。该事件的接收时间就是startTime
	 *            ,endTime参数的值。如果取值<0，则忽略
	 * @param endEventID
	 *            结束事件的ID,返回列表中不包括本事件。从endEventID前面的事件开始获取。该事件的接收时间就是startTime,
	 *            endTime参数的值。如果取值<0，则忽略
	 * @Param maxEventCount
	 *        列表中返回的最大事件对像数目。如果maxEventCount<0,则不限制取回的事件个数。这些事件都从endTime端起计数。
	 * @return 返回结果中的第一个元素为事件个数，从第二个是一个List<SIMEventObject>类型的事件列表，
	 *         事件列表中的事件数目最多为maxEventCount，可以小于第一个元素中的事件个数的数值。
	 * 
	 */
	public List getEventsAndCount(long startTime, long endTime,
			String filterID, long startEventID, long endEventID,
			int maxEventCount);

	/**
	 * 计算各等级和事件总数数并获取事件对像列表。
	 * 
	 * @param startTime
	 *            事件接收时间，对应查询时间的起始点。
	 * @param endTime
	 *            事件接收时间，对应查询时间的结束点。
	 * @param filterID
	 *            应用于事件的过滤器id,如果系统中不存在指定的过滤器，则取回时间段内的全部事件对像。如果不存在过滤器，置为null或空字符串
	 *            .
	 * @param startEventID
	 *            开始事件的ID,返回列表中不包括本事件。从startEventID后面的事件开始获取。该事件的接收时间就是startTime
	 *            ,endTime参数的值。如果取值<0，则忽略
	 * @param endEventID
	 *            结束事件的ID,返回列表中不包括本事件。从endEventID前面的事件开始获取。该事件的接收时间就是startTime,
	 *            endTime参数的值。如果取值<0，则忽略
	 * @Param maxEventCount
	 *        列表中返回的最大事件对像数目。如果maxEventCount<0,则不限制取回的事件个数。这些事件都从endTime端起计数。
	 * @return 返回结果为EventResultSet，其中的各项含义详见该类说明。
	 * 
	 */
	public EventResultSet getEventsAndStatistic(long startTime, long endTime,
			String filterID, long startEventID, long endEventID,
			int maxEventCount);

	/**
	 * 获得当前事件对像的下一个[发生在本事件后面的]事件。
	 * 
	 * @param current
	 *            当前事件对像代理。
	 * @return 当前位置的下一个位置的对象代理。
	 */
	public SIMEventProxy getNextEvent(SIMEventProxy current);

	/**
	 * 获得缓存中时间最近的事件对象。
	 * 
	 * @return
	 */
	public SIMEventObject getRecentEvent();

	/**
	 * 获得缓存中时间最早的事件对象。
	 * 
	 * @return
	 */
	public SIMEventObject getLastEvent();
}

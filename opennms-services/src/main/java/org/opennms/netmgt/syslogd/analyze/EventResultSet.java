package org.opennms.netmgt.syslogd.analyze;

import java.io.Serializable;
import java.util.List;

import org.opennms.netmgt.syslogd.parse.SIMEventObject;

public class EventResultSet implements Serializable {

	/**
	 * 事件总数
	 */
	private long totalEventCount;
	/**
	 * 事件流量总合
	 */
	private long totalFluxCount;
	/**
	 * 按照事件等级划分后，各个等级的事件数量。数组下标代表等级。比如priorityEventCount[0]=100，表示等级为0的事件数是100
	 */
	private long[] priorityEventCount;// 0-4,5 5表示其它
	/**
	 * 按照事件等级划分后，各个等级的事件流量。数组下标代表等级。比如priorityFluxCount[0]=50，表示等级为0的事件流量是50
	 */
	private long[] priorityFluxCount;// 0-4,5 5表示其它

	/**
	 * 返回的实际事件结果集。列表的长度小于等于规定的结果集的最大数目，因此不一定等于totalEventCount的值。
	 */
	private List<SIMEventObject> events;

	public List<SIMEventObject> getEvents() {
		return events;
	}

	public void setEvents(List<SIMEventObject> events) {
		this.events = events;
	}

	public long[] getPriorityEventCount() {
		return priorityEventCount;
	}

	public void setPriorityEventCount(long[] priorityEventCount) {
		this.priorityEventCount = priorityEventCount;
	}

	public long[] getPriorityFluxCount() {
		return priorityFluxCount;
	}

	public void setPriorityFluxCount(long[] priorityFluxCount) {
		this.priorityFluxCount = priorityFluxCount;
	}

	public long getTotalEventCount() {
		return totalEventCount;
	}

	public void setTotalEventCount(long totalEventCount) {
		this.totalEventCount = totalEventCount;
	}

	public long getTotalFluxCount() {
		return totalFluxCount;
	}

	public void setTotalFluxCount(long totalFluxCount) {
		this.totalFluxCount = totalFluxCount;
	}
}

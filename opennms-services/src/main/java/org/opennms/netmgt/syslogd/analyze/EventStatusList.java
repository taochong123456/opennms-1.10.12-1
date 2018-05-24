package org.opennms.netmgt.syslogd.analyze;

import java.util.ArrayList;
import java.util.List;

public class EventStatusList {
	/**
	 * 规则ID
	 */
	private String ruleID;

	/**
	 * 过滤器id。
	 */
	private String filterID;

	/**
	 * 该过滤器保存的最多事件总数。
	 */
	private int capacity = 200;

	/**
	 * 该类表中最早发生的事件时间
	 */
	private long minTime = Long.MAX_VALUE;

	private ArrayList internList = new ArrayList();// @@可以部分持久化到硬盘

	public EventStatusList() {

	}

	public EventStatusList(int capacity) {
		if (capacity > 0)
			this.capacity = capacity;
	}

	/**
	 * 将事件加入此类表
	 * 
	 * @param element
	 */
	public void accept(Object element) {
		if (internList.size() >= capacity) {
			internList.remove(0);
		}
		internList.add(element);
		minTime = Math.min(FilterAssistant.getRecepttime(element), minTime);
	}

	/**
	 * @param i
	 */
	public void remove(int i) {
		internList.remove(i);
	}

	/**
	 * 获取事件列表
	 * 
	 * @return
	 */
	public List getList() {
		return internList;
	}

	/**
	 * 设置列表容量
	 * 
	 * @param capacity
	 */
	public void setCapacity(int capacity) {
		this.capacity = capacity;
		if (internList.size() > capacity) {
			int r = internList.size() - capacity;
			for (int i = 0; i < r; i++) {
				internList.remove(i);
			}
		}
	}

	public String getFilterID() {
		return filterID;
	}

	public void setFilterID(String filterID) {
		this.filterID = filterID;
	}

	/**
	 * 获得列表容量
	 * 
	 * @return
	 */
	public int getCapacity() {
		return capacity;
	}

	public static void main(String[] args) {
		EventStatusList esl = new EventStatusList();
		esl.accept("111");
		esl.accept("222");
		esl.accept("333");
		esl.accept("444");
		esl.accept("555");
		esl.accept("666");
		esl.accept("777");
		esl.accept("888");
		esl.accept("999");
		esl.accept("000");
		List ll = esl.getList();
		for (int i = 0; i < ll.size(); i++) {
			System.out.println(i + ": " + ll.get(i));
		}
	}

	public String getRuleID() {
		return ruleID;
	}

	public void setRuleID(String ruleID) {
		this.ruleID = ruleID;
	}

	public long getMinTime() {
		return minTime;
	}

	public void setMinTime(long minTime) {
		this.minTime = minTime;
	}

}

package org.opennms.netmgt.syslogd.analyze;

import java.util.List;

public class TimeResource implements java.io.Serializable {
	/**
	 * 1 表示NOT，0表示OR，
	 */
	private int logicFlag;

	private int timeFlag;// 值为0表示，每月的第几天；值为1，表示每月第几周的星期几。

	// star系列表示开始时间定义，如果startYear==100，表示每年；如果startYear==100并且startMonth==100表示每年每月，startWeek=100每周，startDay=100每天以此类推；100表示每小时，每分，每秒
	private int startYear;
	private int startMonth;// 1表示1月，12表示2月
	private int startWeek;// 当timeFlag==1时起作用
	private int startDay;// timeFlag==0时表示每月的第几天；timeFlag==1时，表示每周的星期几，1表示星期日，2表示星期一，7表示星期六。
	private int startHour;// 24小时制
	private int startMinute;
	private int startSecond;
	// end系列表示结束时间定义，与start系列一一对应
	private int endYear;
	private int endMonth;
	private int endWeek;// 当timeFlag==1时起作用
	private int endDay;// timeFlag==0时表示每月的第几天；timeFlag==1时，表示每周的星期几，1表示星期日，2表示星期一，7表示星期六。
	private int endHour;// 24小时制
	private int endMinute;
	private int endSecond;

	/**
	 * 各段时间的列表和，如果segments为null表示start，end系列起作用；如果segments不为null,表示segments起作用
	 */
	private List<TimeResource> segments;

	public int getEndDay() {
		return endDay;
	}

	public void setEndDay(int endDay) {
		this.endDay = endDay;
	}

	public int getEndHour() {
		return endHour;
	}

	public void setEndHour(int endHour) {
		this.endHour = endHour;
	}

	public int getEndMinute() {
		return endMinute;
	}

	public void setEndMinute(int endMinute) {
		this.endMinute = endMinute;
	}

	public int getEndMonth() {
		return endMonth;
	}

	public void setEndMonth(int endMonth) {
		this.endMonth = endMonth;
	}

	public int getEndSecond() {
		return endSecond;
	}

	public void setEndSecond(int endSecond) {
		this.endSecond = endSecond;
	}

	public int getEndWeek() {
		return endWeek;
	}

	public void setEndWeek(int endWeek) {
		this.endWeek = endWeek;
	}

	public int getEndYear() {
		return endYear;
	}

	public void setEndYear(int endYear) {
		this.endYear = endYear;
	}

	public int getLogicFlag() {
		return logicFlag;
	}

	public void setLogicFlag(int logicFlag) {
		this.logicFlag = logicFlag;
	}

	public List<TimeResource> getSegments() {
		return segments;
	}

	public void setSegments(List<TimeResource> segments) {
		this.segments = segments;
	}

	public int getStartDay() {
		return startDay;
	}

	public void setStartDay(int startDay) {
		this.startDay = startDay;
	}

	public int getStartHour() {
		return startHour;
	}

	public void setStartHour(int startHour) {
		this.startHour = startHour;
	}

	public int getStartMinute() {
		return startMinute;
	}

	public void setStartMinute(int startMinute) {
		this.startMinute = startMinute;
	}

	public int getStartMonth() {
		return startMonth;
	}

	public void setStartMonth(int startMonth) {
		this.startMonth = startMonth;
	}

	public int getStartSecond() {
		return startSecond;
	}

	public void setStartSecond(int startSecond) {
		this.startSecond = startSecond;
	}

	public int getStartWeek() {
		return startWeek;
	}

	public void setStartWeek(int startWeek) {
		this.startWeek = startWeek;
	}

	public int getStartYear() {
		return startYear;
	}

	public void setStartYear(int startYear) {
		this.startYear = startYear;
	}

	public int getTimeFlag() {
		return timeFlag;
	}

	public void setTimeFlag(int timeFlag) {
		this.timeFlag = timeFlag;
	}

}

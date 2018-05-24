package org.opennms.netmgt.syslogd.analyze;

import java.io.Serializable;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Date;

public class MActionObject implements Serializable {

	/**
	 * 开始执行的具体时间，如果为0则该属性不起作用，执行时间由delayTime决定。
	 */
	private long startTime = 0;

	/**
	 * 延迟执行的时间,如果为0则立即执行，如果startTime不为0则该属性不起作用。
	 */
	private long delayTime = 0;

	/**
	 * 每次执行间隔时间的类型，对应时间单位。
	 */
	private int timeType = Calendar.SECOND;

	/**
	 * 每次执行间隔的时间值，单位由timeType定义。
	 */
	private int timeNum = 1;

	/**
	 * 此调度任务的执行次数，0为循环执行。
	 */
	private int taskNum = 1;

	/**
	 * 动作执行的位置，控制台执行或服务器段执行。缺省为在两端执行。
	 */
	private String position;

	/**
	 * 动作类型，可能是邮件、脚本等等。
	 */
	private String type;

	/**
	 * 执行工作需要的参数列表。原则上params[x][0]对应参数名，params[x][1]对应参数值。
	 */
	private String[][] params;

	public long getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(long delayTime) {
		this.delayTime = delayTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public int getTaskNum() {
		return taskNum;
	}

	public void setTaskNum(int taskNum) {
		this.taskNum = taskNum;
	}

	public int getTimeNum() {
		return timeNum;
	}

	public void setTimeNum(int timeNum) {
		this.timeNum = timeNum;
	}

	public int getTimeType() {
		return timeType;
	}

	public void setTimeType(int timeType) {
		this.timeType = timeType;
	}

	public String[][] getParams() {
		return params;
	}

	public void setParams(String[][] params) {
		this.params = params;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(" type:" + type);
		buffer.append(" position:" + position);
		buffer.append(" startTime:" + new Date(startTime));
		buffer.append(" delayTime:" + delayTime);
		buffer.append(" timeType:" + timeType);
		buffer.append(" timeNum:" + timeNum);
		buffer.append(" taskNum:" + taskNum);
		if (params != null) {
			for (int i = 0; i < params.length; i++) {
				buffer.append(" param:");
				for (int j = 0; j < params[i].length; j++) {
					buffer.append(" " + params[i][j]);
				}
			}
		}
		return buffer.toString();
	}

}

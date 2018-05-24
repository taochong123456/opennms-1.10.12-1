package org.opennms.netmgt.syslogd.analyze;

public class TimeScope implements java.io.Serializable {

	private int length = 5;// 时间范围，时间窗口大小，<=0被认为没有设置时间窗口，默认为5

	private String unit = "min";// 时间单位，可以取值"min","Hour","Day","Week"
								// 不区分大小写,如果是不支持的时间范围默认为5分钟

	private int frequency = 1;// 在时间段内，发生的次数。事件发生频率，缺省为1次。

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	/**
	 * 获得时间间隔，单位ms
	 * 
	 * @return
	 */
	public long getTimeInterval() {

		return 0;
	}

}

package org.opennms.netmgt.syslogd.parse;

import java.text.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventFieldPattern {
	public static int BASETIME_YEAR = 1000;

	private long baseTime = 0;
	private String dvalue = null;
	private EventFieldExpression exp = null;// added by zhuzhen 20110527.
											// �����¼����Եı��ʽ����
	private Format format = null;
	private HashMap keyValue = null;
	private int[] matchIndex = null;
	private String name = null;
	private Object value = null;
	private String[] valueMatch = null;
	private Pattern[] valuePattern = null;

	public long getBaseTime() {
		return baseTime;
	}

	/**
	 * ����Ĭ��ֵ
	 * 
	 * @return
	 */
	public String getDefaultValue() {
		return this.dvalue;
	}

	public EventFieldExpression getExp() {
		return exp;
	}

	/**
	 * ��ȡ��ʽ���ַ�
	 * 
	 * @return
	 */
	public Format getFormat() {
		return format;
	}

	/**
	 * ��ȡӳ���ֶε�ֵ��
	 * 
	 * @return
	 */
	public String getFormatValue(String fvalue) {
		if (fvalue != null) {
			if (keyValue != null) {
				return (String) keyValue.get(fvalue);
			}

			for (int i = 0; i < valuePattern.length; i++) {
				Matcher m = valuePattern[i].matcher(fvalue);
				if (m.find()) {
					if (this.valueMatch[i] == null)
						return m.group(m.groupCount());
					else
						return this.valueMatch[i];
				}
			}
		} else {
			if (this.dvalue != null)
				return this.dvalue;
		}
		return null;
	}

	/**
	 * ��ȡƥ�������
	 * 
	 * @return
	 */
	public int[] getMatchIndex() {
		return matchIndex;
	}

	/**
	 * ��ȡ�ֶε����
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * ��ȡӳ�䷽ʽ
	 * 
	 * @return
	 */
	public Object getValue() {
		return this.value;
	}

	public void setBaseTime(long baseTime) {
		this.baseTime = baseTime;
	}

	/**
	 * ����Ĭ��ֵ
	 * 
	 * @param value
	 */
	public void setDefaultValue(String value) {
		this.dvalue = value;
	}

	public void setExp(EventFieldExpression exp) {
		this.exp = exp;
	}

	/**
	 * ���ø�ʽ���ַ�
	 * 
	 * @param format
	 */
	public void setFormat(Format format) {
		this.format = format;
	}

	/**
	 * ����ƥ�������
	 * 
	 * @param matchIndex
	 */
	public void setMatchIndex(int[] matchIndex) {
		this.matchIndex = matchIndex;
	}

	/**
	 * �����ֶε����
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * ����ӳ���ֶε�ֵ
	 * 
	 * @param value
	 */
	public void setValue(Object value) {
		this.value = value;
		if (value instanceof Object[]) {
			Object[] obs = (Object[]) value;
			this.valuePattern = new Pattern[obs.length];
			this.valueMatch = new String[obs.length];
			for (int i = 0; i < obs.length; i++) {
				String[] matchs = (String[]) obs[i];
				valuePattern[i] = Pattern.compile(matchs[0]);
				if (matchs[1].equals("")) {
					valueMatch[i] = null;
				} else {
					valueMatch[i] = matchs[1];
				}
			}
		} else if (value instanceof HashMap) {
			keyValue = (HashMap) value;
		}
	}

	@Override
	public String toString() {
		return "EventFieldPattern [name=" + name + ", format=" + format
				+ ", value=" + value + ", dvalue=" + dvalue + ", valuePattern="
				+ Arrays.toString(valuePattern) + ", valueMatch="
				+ Arrays.toString(valueMatch) + ", keyValue=" + keyValue
				+ ", matchIndex=" + Arrays.toString(matchIndex) + ", baseTime="
				+ baseTime + ", exp=" + exp + "]";
	}

}

package org.opennms.netmgt.syslogd.common;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Constants {
	/**
	 * 不要重复事件判断
	 */
	public final static int NO_REPEATED=0;
	/**
	 * 需要进行重复事件判断，但不需要进行更精细的判断
	 */
	public final static int SINGLE_REPEATED=1;
	/**
	 * 需要进行重复事件判断，并且这些事件的某些属性都相同[分组]
	 */
	public final static int GROUP_REPEATED=2;
	/**
	 * 需要进行重复事件判断，并且这些事件的某些属性都不同
	 */
	public final static int DISTINCT_REPEATED=3;
	/**
	 * 需要进行重复事件判断，并且这些事件的某些属性都相同[分组]，某些属性都不同
	 */
	public final static int COMPLEX_REPEATED=4;
	
	
	/**
	 * 规则支持的时间单位
	 */
	public final static Set SupportedTimeUnit = new LinkedHashSet();
	static{
		SupportedTimeUnit.add("sec");
		SupportedTimeUnit.add("min");
		SupportedTimeUnit.add("hour");
		SupportedTimeUnit.add("day");
		SupportedTimeUnit.add("week");
	}
	/**
	 * 时间单位和ms的对应关系。
	 */
	public final static Map<String,Long> TimeUnitRelation = new LinkedHashMap<String,Long>();
	static{
		TimeUnitRelation.put("sec", new Long(1*1000));
		TimeUnitRelation.put("min", new Long(1*60*1000));
		TimeUnitRelation.put("hour", new Long(1*60*60*1000));
		TimeUnitRelation.put("day", new Long(1*24*60*60*1000));
		TimeUnitRelation.put("week", new Long(1*7*24*60*60*1000));
	}
	
}

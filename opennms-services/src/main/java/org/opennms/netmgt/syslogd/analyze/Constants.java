package org.opennms.netmgt.syslogd.analyze;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Constants {
	/**
	 * varbinds的type类型之常量类型。
	 */
	public static final String VAR_CONSTANT = "constant";
	/**
	 * varbinds的type类型之事件类型。
	 */
	public static final String VAR_EVENT = "event";
	/**
	 * varbinds的type类型之过滤器类型。
	 */
	public static final String VAR_FILTER = "filter";
	/**
	 * varbinds的type类型之过滤器插件类型，过滤器插件是引入的第三方判断逻辑。
	 */
	public static final String VAR_FILTERPLUGIN = "filterplugin";

	/**
	 * varbinds的type类型之过滤器类型。
	 */
	public static final String VAR_RULE = "rule";
	/**
	 * varbinds的type类型之资源引用类型。
	 */
	public static final String VAR_RESOURCE = "resource";
	/**
	 * varbinds的type类型之内部使用类型。
	 */
	public static final String VAR_INTERN = "intern";
	/**
	 * varbinds的type类型之黑白名单类型。
	 */
	public static final String VAR_BWLIST = "bwlist";

	// -- 支持的操作符 --
	/**
	 * 逻辑运算符常量，“相等,数字相等”
	 */
	public final static String OPR_EQ = "eq";//
	/**
	 * 逻辑运算符常量，“不等"!="”
	 */
	public final static String OPR_NE = "ne";//
/**
	 * 逻辑运算符常量，“小于"<"”
	 */
	public final static String OPR_LT = "lt";//
	/**
	 * 逻辑运算符常量，“小于等于"<="”
	 */
	public final static String OPR_LE = "le";//
	/**
	 * 逻辑运算符常量，“大于">"”
	 */
	public final static String OPR_GT = "gt";//
	/**
	 * 逻辑运算符常量，“大于等于">="”
	 */
	public final static String OPR_GE = "ge";//
	/**
	 * 逻辑运算符常量，“范围之间”
	 */
	public final static String OPR_BETWEEN = "between";//
	/**
	 * 逻辑运算符常量，“属于,即属于一组数值”
	 */
	public final static String OPR_INNUMBER = "innumber";//
	/**
	 * 逻辑运算符常量，“相等,即字符串相等”
	 */
	public final static String OPR_EQSTR = "eq_str";//

	/**
	 * 逻辑运算符常量，“不相等,即字符串不相等”
	 */
	public final static String OPR_NESTR = "ne_str";//
	/**
	 * 逻辑运算符常量，“相等,即字符串相等,忽略大小写”
	 */
	public final static String OPR_EQSTR_IC = "eq_str_ic";//
	/**
	 * 逻辑运算符常量，“属于,即属于一组字符串”
	 */
	public final static String OPR_IN = "in";//
	/**
	 * 逻辑运算符常量，“属于,即属于一组字符串,忽略大小写”
	 */
	public final static String OPR_IN_IC = "in_ic";//
	/**
	 * 逻辑运算符常量，“开始于”
	 */
	public final static String OPR_STARTSWITH = "startswith";//
	/**
	 * 逻辑运算符常量，“开始于,忽略大小写”
	 */
	public final static String OPR_STARTSWITH_IC = "startswith_ic";//
	/**
	 * 逻辑运算符常量，“结束于”
	 */
	public final static String OPR_ENDSWITH = "endswith";//
	/**
	 * 逻辑运算符常量，“结束于,忽略大小写”
	 */
	public final static String OPR_ENDSWITH_IC = "endswith_ic";//
	/**
	 * 逻辑运算符常量，“匹配,即正则表达式匹配”
	 */
	public final static String OPR_MATCHES = "matches";//
	/**
	 * 逻辑运算符常量，“匹配,即正则表达式匹配,忽略大小写”
	 */
	public final static String OPR_MATCHES_IC = "matches_ic";//
	/**
	 * 逻辑运算符常量，“包含,对于A contains B,B可以是一个字符串长量或正则表达式”
	 */
	public final static String OPR_CONTAINS = "contains";//
	/**
	 * 逻辑运算符常量，“包含,对于A contains B,B可以是一个字符串长量或正则表达式,忽略大小写”
	 */
	public final static String OPR_CONTAINS_IC = "contains_ic";//
	/**
	 * 逻辑运算符常量，“属于,即属于一组子网”
	 */
	public final static String OPR_INSUBNET = "insubnet";//
	/**
	 * 逻辑运算符常量，“属于,即属于分类,主要用于某资产具有[或属于]某些属性”
	 */
	public final static String OPR_INCATEGORY = "incategory";//
	/**
	 * 逻辑运算符常量，“是否为空, 运算结果对应的值为字符串yes或no”
	 */
	public final static String OPR_ISNULL = "isnull";//
	/**
	 * 逻辑运算符常量，“是否满足过滤器”
	 */
	public final static String OPR_MATCHESFILTER = "matchesfilter";//
	/**
	 * 逻辑运算符常量，“是否满足IP地址资源”
	 */
	public final static String OPR_MATCHES_ADDR_RESOURCE = "matches_addr_resource";
	/**
	 * 逻辑运算符常量，“是否满足端口或服务资源”
	 */
	public final static String OPR_MATCHES_PORT_RESOURCE = "matches_port_resource";
	/**
	 * 逻辑运算符常量，“是否满足时间资源”
	 */
	public final static String OPR_MATCHES_TIME_RESOURCE = "matches_time_resource";
	/**
	 * 逻辑运算符常量，“是否触发指定规则的事件”。该操作符号并没有对应的运算，只是当FilterDelegate初始化时，
	 * 会将该运算转换为其他等价运算节点。
	 */
	public final static String OPR_MATCHESRULE = "matchesrule";
	/**
	 * 逻辑运算符常量，“是否触发指定的过滤器插件”。
	 */
	public final static String OPR_MATCHESFILTERPLUGIN = "matchesfilterplugin";
	/**
	 * 逻辑运算符常量，“是否属于指定的黑白名单”。
	 */
	public final static String OPR_MATCHESBWLIST = "matchesbwlist";

	/**
	 * 非单目操作数的逻辑运算符集合。指操作数对应的操作数不是单独的值，比如a
	 * between(10,20).10和20就是between的右操作数，这个操作数由两个数字组成，不是单值。
	 */
	public final static Set nonSingleOprands = new HashSet();
	static {
		nonSingleOprands.add(OPR_BETWEEN);
		nonSingleOprands.add(OPR_INNUMBER);
		nonSingleOprands.add(OPR_IN);
		nonSingleOprands.add(OPR_IN_IC);
		nonSingleOprands.add(OPR_INSUBNET);
		nonSingleOprands.add(OPR_INCATEGORY);
	}

	public final static Set<String> stringOperators = new HashSet<String>();
	static {
		stringOperators.add(OPR_EQSTR);
		stringOperators.add(OPR_EQSTR_IC);
		stringOperators.add(OPR_IN);
		stringOperators.add(OPR_IN_IC);
		stringOperators.add(OPR_STARTSWITH);
		stringOperators.add(OPR_STARTSWITH_IC);
		stringOperators.add(OPR_ENDSWITH);
		stringOperators.add(OPR_ENDSWITH_IC);
		stringOperators.add(OPR_MATCHES);
		stringOperators.add(OPR_MATCHES_IC);
		stringOperators.add(OPR_CONTAINS);
		stringOperators.add(OPR_CONTAINS_IC);
	}

	public final static Set<String> sqlTrans = new HashSet<String>();

	/**
	 * Add by liyue 2013-1-28 增加SIMEventObject的地址类属性集合
	 */
	public final static Set<String> addressFieldSet = new HashSet<String>();
	static {
		addressFieldSet.add("collectoraddr");
		addressFieldSet.add("saddr");
		addressFieldSet.add("staddr");
		addressFieldSet.add("daddr");
		addressFieldSet.add("dtaddr");
		addressFieldSet.add("devaddr");
	}

	/**
	 * 逻辑表达式符号,“与”
	 */
	public final static String EXP_AND = "and";//
	/**
	 * 逻辑表达式符号,“或”
	 */
	public final static String EXP_OR = "or";// 逻辑表达式符号
	/**
	 * 逻辑表达式符号,“非”
	 */
	public final static String EXP_NOT = "not";// 逻辑表达式符号
	/**
	 * 逻辑表达式符号,“原子”表达式
	 */
	public final static String EXP_BASE = "base";// 逻辑表达式符号

	public final static String TEMP_FILTERID_PREFIX = "$temp_";// 临时过滤器的id的前缀

	public final static Map<String, String> sqlOperatorsMap = new HashMap<String, String>();
	static {
		sqlOperatorsMap.put(OPR_EQ, "=");
		sqlOperatorsMap.put(OPR_NE, "<>");
		sqlOperatorsMap.put(OPR_LT, "<");
		sqlOperatorsMap.put(OPR_LE, "<=");
		sqlOperatorsMap.put(OPR_GT, ">");
		sqlOperatorsMap.put(OPR_GE, ">=");
		sqlOperatorsMap.put(OPR_INNUMBER, "in");
		sqlOperatorsMap.put(OPR_EQSTR, "=");
		sqlOperatorsMap.put(OPR_IN, "in");
		sqlOperatorsMap.put(OPR_BETWEEN, "");
		sqlOperatorsMap.put(OPR_ISNULL, "");
		sqlOperatorsMap.put(OPR_STARTSWITH, "");
		sqlOperatorsMap.put(OPR_ENDSWITH, "");
		sqlOperatorsMap.put(OPR_CONTAINS, "");
	}

	public static String TimeStampName = "recepttime"; // 传入事件中标识事件接收时间的参数名字

	public static String BWListID = "blackWhiteList"; // 黑白名单运算对应的插件ID，该值存在与filterconfig.xml中

	public static final int IP_RES_TYPE = 1;// IP资源类型

	public static final int PORT_RES_TYPE = 2;// 端口资源类型

	public static final int TIME_RES_TYPE = 3;// 时间资源类型

}

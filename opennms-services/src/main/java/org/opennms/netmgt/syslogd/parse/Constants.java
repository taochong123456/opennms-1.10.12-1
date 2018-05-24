package org.opennms.netmgt.syslogd.parse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Constants {
	public static final String VAR_CONSTANT = "constant";
	public static final String VAR_EVENT = "event";
	public static final String VAR_FILTER = "filter";
	public static final String VAR_FILTERPLUGIN = "filterplugin";

	public static final String VAR_RULE = "rule";
	public static final String VAR_RESOURCE = "resource";

	public static final String VAR_INTERN = "intern";
	public static final String VAR_BWLIST = "bwlist";
	public final static String OPR_EQ = "eq";//
	public final static String OPR_NE = "ne";//
	public final static String OPR_LT = "lt";//
	public final static String OPR_LE = "le";//
	public final static String OPR_GT = "gt";//
	public final static String OPR_GE = "ge";//
	public final static String OPR_BETWEEN = "between";//
	public final static String OPR_INNUMBER = "innumber";//
	public final static String OPR_EQSTR = "eq_str";//
	public final static String OPR_NESTR = "ne_str";//
	public final static String OPR_EQSTR_IC = "eq_str_ic";//
	public final static String OPR_IN = "in";//
	public final static String OPR_IN_IC = "in_ic";//
	public final static String OPR_STARTSWITH = "startswith";//
	public final static String OPR_STARTSWITH_IC = "startswith_ic";//

	public final static String OPR_ENDSWITH = "endswith";//

	public final static String OPR_ENDSWITH_IC = "endswith_ic";//

	public final static String OPR_MATCHES = "matches";//

	public final static String OPR_MATCHES_IC = "matches_ic";//

	public final static String OPR_CONTAINS = "contains";//

	public final static String OPR_CONTAINS_IC = "contains_ic";//

	public final static String OPR_INSUBNET = "insubnet";//

	public final static String OPR_INCATEGORY = "incategory";//

	public final static String OPR_ISNULL = "isnull";//

	public final static String OPR_MATCHESFILTER = "matchesfilter";//

	public final static String OPR_MATCHES_ADDR_RESOURCE = "matches_addr_resource";

	public final static String OPR_MATCHES_PORT_RESOURCE = "matches_port_resource";

	public final static String OPR_MATCHES_TIME_RESOURCE = "matches_time_resource";

	public final static String OPR_MATCHESRULE = "matchesrule";

	public final static String OPR_MATCHESFILTERPLUGIN = "matchesfilterplugin";

	public final static String OPR_MATCHESBWLIST = "matchesbwlist";

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

	public final static Set<String> addressFieldSet = new HashSet<String>();
	static {
		addressFieldSet.add("collectoraddr");
		addressFieldSet.add("saddr");
		addressFieldSet.add("staddr");
		addressFieldSet.add("daddr");
		addressFieldSet.add("dtaddr");
		addressFieldSet.add("devaddr");
	}

	public final static String EXP_AND = "and";//

	public final static String EXP_OR = "or";//

	public final static String EXP_NOT = "not";//

	public final static String EXP_BASE = "base";//

	public final static String TEMP_FILTERID_PREFIX = "$temp_";//

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

	public static String TimeStampName = "recepttime"; //

	public static String BWListID = "blackWhiteList"; //

	public static final int IP_RES_TYPE = 1;//

	public static final int PORT_RES_TYPE = 2;//

	public static final int TIME_RES_TYPE = 3;//

}

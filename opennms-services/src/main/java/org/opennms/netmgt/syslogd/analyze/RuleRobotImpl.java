package org.opennms.netmgt.syslogd.analyze;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.opennms.netmgt.syslogd.parse.SIMEventObject;

public class RuleRobotImpl implements RuleRobot {

	public static long DefaultWindowSize = 5 * 60 * 1000;// 缺省的时间窗口大小，5分钟，以ms计。

	// public static String EVENTID = "id"; // 传入事件中标识事件id的参数名字.是小写

	/**
	 * 该值作为判断是否需要清除过时数据的标准。该值与规则中设定的时间窗口重复事件的数目没有任何关系。 该值用于保证进行计算的数据库表保持正常运转状态。
	 * 它的含义是本规则每发现THRESHOLD次重复匹配后就清一次超过时间窗口的过时数据。
	 */
	public static int THRESHOLD = 50000;

	/**
	 * 相邻预警触发的最短时间间隔常量，值为0，表示全部触发。
	 */
	public static final long FIREEVERYTIME = 0;

	/**
	 * 规则对像
	 */
	SIMRuleObject rule;

	/**
	 * 事件关联过滤器
	 */
	FilterDelegate associationFilter;

	/**
	 * point基本事件过滤器
	 */
	Map<String, FilterDelegate> pointFilterDelegateMaps = new LinkedHashMap<String, FilterDelegate>();

	/**
	 * 两次预警间的最短触发间隔。单位ms。
	 */
	long fireInterval;

	/**
	 * 上次事件匹配成功的时间
	 */
	long lastSatisfiedTime;

	/**
	 * 本次事件匹配成功的时间
	 */
	long currentSatisfiedTime;

	/**
	 * 重复事件的时间窗口大小
	 */
	long windowTimeSize;

	/**
	 * 重复事件判断的标志
	 */
	int repeatedFlag;

	/**
	 * 内置的重复事件的数据库表名.该表的每一行由发生的事件id[或前后关联的一组事件id]和时间戳组成。
	 * 表名由"rr_"和规则id号首部和规则号hashcode三部分组成。
	 */
	String mainTableName;

	/**
	 * 成功匹配的重复事件/事件组的计数。当规则中没有设置identical和different时的已经匹配的重复事件计数。
	 * 它的作用主要是提高规则判断的效率，首先singleRepeatedEventsCount超过规则设定的重复事件数目时，
	 * 才会进行数据库查询实际的重复事件数目。
	 */
	private long singleRepeatedEventsCount;

	/**
	 * 本规则随服务器启动以来识别的重复事件数目，该值循环使用，达到THRESHOLD时将要清零，重新开始计数。
	 * 它的作用就是当收到指定数目THRESHOLD的重复事件时，试图清空一次相关的数据库表，以保证相关的数据表不至于过大，同时也能提高其他模块的查询效率
	 */
	private long repeatedEventsCount;

	/**
	 * 当前处理的事件对像的接收时间
	 */
	private long currentEventTime;

	/**
	 * 当前实际接收的事件参数。也是当前事件发生时间的纪录点。当isSatisfied()函数被调用时，该变量被更新。
	 */
	private Object currentEvent;

	/**
	 * 变量的实际参数区.当前最新满足pointFilterDelegateMaps中过滤器的事件，需要进行关联判断的一组事件。
	 */
	private List<SIMEventParameter> pointEvents = new ArrayList<SIMEventParameter>();

	/**
	 * 变量的实际参数区.记录的是满足过滤器的历史事件对象。String表示基本过滤器的id
	 */
	private Map<String, EventStatusList> pointEventHistoryMap = new HashMap<String, EventStatusList>();

	/**
	 * 满足规则的事件轨迹.每一个元素都是List<Long>,如果规则是多事件关联的，那么每个List<Long>表示这样一组事件
	 */
	private List<List<Long>> eventTrails;

	public final static int CACHED_FILTER_SATISFIEDARRAY_LENGTH = 500;

	/**
	 * 缓存500个最近成功匹配的事件号。这里只要满足某个过滤器就算，不一定非要满足整个规则，它记录了在规则判断中“起作用”的事件。
	 * 主要用来判断是否使用过某事件。
	 * 
	 */
	private long[] filterSatisfiedArray = new long[CACHED_FILTER_SATISFIEDARRAY_LENGTH];

	/**
	 * filterSatisfiedID数组的下标。
	 */
	private int satisfiedIDIndex = 0;

	/**
	 * 构造函数，用于创建RuleDelegate
	 * 
	 * @param rule
	 * @param needReconstruct
	 *            对于重复事件判断的情况是否需要创建相关的数据库表。 如果是通过用户界面添加或更新规则时，需要设置为true
	 *            服务器系统启动初始化时[这时已经有数据库表存在了]，需要将其设置为false。
	 *            注：对于重复事件的判断根据规则是否设置了相同事件属性和不同事件属性，而分为简单规则和复杂规则两种情形。
	 *            这两种情形都需要使用数据库来记录状态。
	 */
	public RuleRobotImpl(SIMRuleObject rule, boolean needReconstruct) {
		// 规则对象必须不为空。
		if (rule == null)
			throw new NullPointerException(
					"Rule object is null. Such delegate can not be constructed!");
		this.rule = rule;

		// TestingEnvironment.printRuleObjectContent(rule);
		// -- 建立过滤区，包括pattern和point过滤区 --
		VarBinds patternVar = rule.getPatternVar();// patternVar是关联过滤器的表示。
		try {
			if (patternVar != null
					&& StringTools.hasContent(patternVar.getRef())) {
				associationFilter = new FilterDelegateImpl(rule.getFilterMaps()
						.get(patternVar.getRef()));
				// System.out.println("assi:"+assosiationFilter.getFilterObject().getId());
			}
		} catch (Exception e) {
			logger.error(
					"Can not create associationFilter when constructing ruledelegate ",
					e);
		}
		List<VarBinds> pointVars = rule.getPointVars();
		try {
			if (pointVars != null && pointVars.size() > 0) {
				for (int i = 0; i < pointVars.size(); i++) {
					VarBinds var = pointVars.get(i);
					String ref = var.getRef();
					if (StringTools.hasContent(ref)) {
						FilterDelegate filterDelegate = new FilterDelegateImpl(
								rule.getFilterMaps().get(ref));

						// 设置过滤器的引用。
						pointFilterDelegateMaps.put(ref, filterDelegate);

						// 设置过滤器的事件列表。
						EventStatusList esl = new EventStatusList();
						esl.setRuleID(rule.getRuleID());
						esl.setFilterID(ref);
						pointEventHistoryMap.put(ref, esl);

						// System.out.println("point:"+filterDelegate.getFilterObject().getId());
					}
				}
			}
		} catch (Exception e) {
			pointFilterDelegateMaps = new LinkedHashMap<String, FilterDelegate>();// 还原
			logger.error(
					"Can not create pointFilters when constructing ruledelegate ",
					e);

		}

		// -- 计算预警报告间隔时间/该规则触发的两个报警之间的最小时间间隔 --
		fireInterval = calFireInterval();
		// -- 计算重复事件时间窗口的大小 --
		windowTimeSize = calWindowTimeSize();

		// 设置潜在的数据库表名
		if (rule.getRuleID() == null)
			throw new NullPointerException(
					"Rule ID is null. Such delegate can not be constructed!");
		String ruleID = rule.getRuleID().replace('-', '_');// 如果表名中含有'-',插入数据时,可能会应为表名的原因而报错。

		mainTableName = "rr_" + ruleID.split("_")[0] + "_"
				+ Math.abs(ruleID.hashCode());

		// -- 如果需要进行重复次数验证，则建立repeatedFlag状态 --
		TimeScope ts = rule.getTimeScope();
		if (ts == null)
			return;
		if (ts.getFrequency() <= 1) {// 没有重复事件判断
			repeatedFlag = org.opennms.netmgt.syslogd.common.Constants.NO_REPEATED;
			removePreviousTable();// 有可能是规则更新的操作，因此有可能重复事件判断改为了单事件判断
		} else {// 有重复事件判断要求

			// -- 判断重复的种类 --
			repeatedFlag = org.opennms.netmgt.syslogd.common.Constants.SINGLE_REPEATED;// 先赋予缺省值
			if (hasElement(rule.getIdVars()) && !hasElement(rule.getDiffVars())) {
				repeatedFlag = org.opennms.netmgt.syslogd.common.Constants.GROUP_REPEATED;
			}
			if (!hasElement(rule.getIdVars()) && hasElement(rule.getDiffVars())) {
				repeatedFlag = org.opennms.netmgt.syslogd.common.Constants.DISTINCT_REPEATED;
			}
			if (hasElement(rule.getIdVars()) && hasElement(rule.getDiffVars())) {
				repeatedFlag = org.opennms.netmgt.syslogd.common.Constants.COMPLEX_REPEATED;
			}
			if (needReconstruct)// 需要重构数据库表时，
				makeRuleTables(repeatedFlag);
			else {// 错误处理或其他异常状态处理。进入到“else {// 有重复事件判断要求”分支的都必须有数据表对应。
				if (!mainTableExist())// 不需要重构数据库表，并且也没有存在数据库表时。说明状态不一致，需要更正。
					makeRuleTables(repeatedFlag);
			}

			if (repeatedFlag == org.opennms.netmgt.syslogd.common.Constants.SINGLE_REPEATED) {
				singleRepeatedEventsCount = countInitialRepeatedEvents(repeatedFlag);
				// System.out.println("init singlerepeatedEventsCount:"
				// + singleRepeatedEventsCount);
			}
		}

		// TestingEnvironment.printRuleObjectContent(rule);

	}

	/**
	 * 日志接口。
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	public SIMRuleObject getRuleObject() {
		return rule;
	}

	/**
	 * 释放资源。
	 */
	public void release() {

	}

	public boolean isSatisfied(SIMEventObject event) {
		if (event == null)
			return false;
		if (isSatisfiedInline(event)) {
			// lastSatisfiedTime = currentSatisfiedTime;
			currentSatisfiedTime = (Long) currentEventTime;
			return shouldFire();
		} else
			return false;

	}

	public boolean isSatisfied(Map eventParams) {
		if (eventParams == null)
			return false;
		if (isSatisfiedInline(eventParams)) {
			// lastSatisfiedTime = currentSatisfiedTime;
			currentSatisfiedTime = (Long) currentEventTime;
			return shouldFire();
		} else
			return false;
	}

	/**
	 * 判断是否满足触发预警的时间间隔条件
	 */
	public boolean shouldFire() {
		if (fireInterval == FIREEVERYTIME)
			return true;
		if ((currentSatisfiedTime - lastSatisfiedTime) >= fireInterval) {
			lastSatisfiedTime = currentSatisfiedTime;
			return true;
		} else
			return false;
	}

	/**
	 * 根据规则中的描述计算重复事件时间窗口的大小。
	 * 
	 * @return
	 */
	private long calWindowTimeSize() {
		TimeScope ts = rule.getTimeScope();
		// -- 没有设置时间窗口时，采用缺省值 --
		if (ts == null)
			return DefaultWindowSize;

		// -- 计算时间窗口 --
		int length = ts.getLength();
		String unit = ts.getUnit();
		Long unitMs = org.opennms.netmgt.syslogd.common.Constants.TimeUnitRelation
				.get(unit);
		if (unitMs == null)// 对于没有定义的时间单位，采用时间窗口的缺省值。
			return DefaultWindowSize;

		return Math.abs(length * unitMs);
	}

	/**
	 * 计算预警触发间隔
	 * 
	 * @return
	 */
	private long calFireInterval() {
		TimeScope fireIntervalScope = rule.getFireInterval();
		if (fireIntervalScope == null)//
			return FIREEVERYTIME;

		// -- 计算时间间隔 --
		int length = fireIntervalScope.getLength();
		String unit = fireIntervalScope.getUnit();
		Long unitMs = org.opennms.netmgt.syslogd.common.Constants.TimeUnitRelation
				.get(unit);
		if (unitMs == null)// 对于没有定义的时间单位，采用缺省值。
			return FIREEVERYTIME;

		return Math.abs(length * unitMs);
	}

	/**
	 * 判断集合是否含有一个或一个以上的元素
	 * 
	 * @param c
	 * @return
	 */
	private boolean hasElement(Collection c) {
		if (c == null || c.size() <= 0)
			return false;
		else
			return true;
	}

	/**
	 * 判断重复事件的主表是否存在。该函数适用于mysql.不具有可移植性。
	 * 
	 * @return
	 */
	private boolean mainTableExist() {
		return true;
	}

	/**
	 * 构造重复事件判断需要的数据库表，建立持久化判断的环境
	 * 
	 * @param flag
	 */
	private void makeRuleTables(int flag) {
	}

	/**
	 * 试着删除原先存在的库表
	 * 
	 */
	private void removePreviousTable() {
	}

	/**
	 * 创建重复事件判断所需要的数据库表。该函数适用于mysql.不具有可移植性。 简单事件表的组成如下所示：说明表中所有的'-'都以被替换为'_'
	 * tablename： rr_规则号 columnnames:
	 * f_filterid1,f_filterid2,...f_filteridN,recepttime index: recepttime
	 * 说明：有几个基本过滤器就会有几个f_filterid字段，f_filterid字段对应满足这些基本过滤器的事件id。
	 */
	private void makeSingleRuleTables() {
	}

	/**
	 * 创建重复事件判断所需要的数据库表。该函数适用于mssql.不具有可移植性。 简单事件表的组成如下所示：说明表中所有的'-'都以被替换为'_'
	 * tablename： rr_规则号 columnnames:
	 * f_filterid1,f_filterid2,...f_filteridN,recepttime index: recepttime
	 * 说明：有几个基本过滤器就会有几个f_filterid字段，f_filterid字段对应满足这些基本过滤器的事件id。
	 */
	private void makeSingleRuleTablesForSqlServer() {
	}

	/**
	 * 创建重复事件判断所需要的数据库表。该函数适用于oracle.不具有可移植性。 简单事件表的组成如下所示：说明表中所有的'-'都以被替换为'_'
	 * tablename： rr_规则号 columnnames:
	 * f_filterid1,f_filterid2,...f_filteridN,recepttime index: recepttime
	 * 说明：有几个基本过滤器就会有几个f_filterid字段，f_filterid字段对应满足这些基本过滤器的事件id。
	 */
	private void makeSingleRuleTablesForOracle() {
	}

	/**
	 * 根据VarBinds获得需要创建的数据库的列名。 列名由三部分组成：f_+过滤器id号的hash值+该过滤器对应的属性名
	 * 
	 * @param var
	 * @return 说明：使用过滤器id号的hash方法是为了缩短列名。 通过getAttrForDB获得的数据库列名将在本规则范围内保持唯一。
	 */
	private String getAttrForDB(VarBinds var) {
		String ref = var.getRef();// ref是规则中关联事件的id
		String attr = var.getAttrname();
		ref = "f_" + String.valueOf(ref.hashCode()).replace('-', '_');
		return ref + "_" + attr;
	}

	/**
	 * 创建重复事件判断所需要的数据库表,包括group和disctinct判断。该函数适用于mysql.不具有可移植性。 tablename：
	 * rr_规则号 columnnames:
	 * f_filterid1,f_filterid2,...f_filteridN,groupcolumn1,groupcolumn2,...
	 * groupcolumnN,distinctcolumn1,distinctcolumn2,...distinctcolumnN,
	 * recepttime index:
	 * recepttime,groupcolumn1,...groupcolumnN,distinctcolumn1,...
	 * distinctcolumnN
	 * 说明：有几个基本过滤器就会有几个f_filterid字段，f_filterid字段对应满足这些基本过滤器的事件id。
	 * 有几个相同属性的字段就有几个groupcolumn字段，有几个不同属性的字段就有几个distinctcolumn字段
	 * 
	 */
	private void makeComplexRuleTables() {
	}

	/**
	 * 创建重复事件判断所需要的数据库表,包括group和disctinct判断。该函数适用于mssql.不具有可移植性。 tablename：
	 * rr_规则号 columnnames:
	 * f_filterid1,f_filterid2,...f_filteridN,groupcolumn1,groupcolumn2,...
	 * groupcolumnN,distinctcolumn1,distinctcolumn2,...distinctcolumnN,
	 * recepttime index:
	 * recepttime,groupcolumn1,...groupcolumnN,distinctcolumn1,...
	 * distinctcolumnN
	 * 说明：有几个基本过滤器就会有几个f_filterid字段，f_filterid字段对应满足这些基本过滤器的事件id。
	 * 有几个相同属性的字段就有几个groupcolumn字段，有几个不同属性的字段就有几个distinctcolumn字段
	 * 
	 */
	private void makeComplexRuleTablesForSqlServer() {
	}

	/**
	 * 创建重复事件判断所需要的数据库表,包括group和disctinct判断。该函数适用于oracle.不具有可移植性。 tablename：
	 * rr_规则号 columnnames:
	 * f_filterid1,f_filterid2,...f_filteridN,groupcolumn1,groupcolumn2,...
	 * groupcolumnN,distinctcolumn1,distinctcolumn2,...distinctcolumnN,
	 * recepttime index:
	 * recepttime,groupcolumn1,...groupcolumnN,distinctcolumn1,...
	 * distinctcolumnN
	 * 说明：有几个基本过滤器就会有几个f_filterid字段，f_filterid字段对应满足这些基本过滤器的事件id。
	 * 有几个相同属性的字段就有几个groupcolumn字段，有几个不同属性的字段就有几个distinctcolumn字段
	 * 
	 */
	private void makeComplexRuleTablesForOracle() {
	}

	/**
	 * 将变量区中的事件，记录到数据库中。该变量区中的事件是某次重复事件中的一次。在数据库中占一条记录
	 * 
	 * @param conn
	 * @param flag
	 */
	private void insertRepeatedEvents(Connection conn, int flag)
			throws SQLException, IllegalAccessException {
		switch (flag) {
		case org.opennms.netmgt.syslogd.common.Constants.SINGLE_REPEATED:
			insertSingleRepeatedEvents(conn);
			break;
		case org.opennms.netmgt.syslogd.common.Constants.GROUP_REPEATED:
			insertComplexRepeatedEvents(conn);
			break;
		case org.opennms.netmgt.syslogd.common.Constants.DISTINCT_REPEATED:
			insertComplexRepeatedEvents(conn);
			break;
		case org.opennms.netmgt.syslogd.common.Constants.COMPLEX_REPEATED:
			insertComplexRepeatedEvents(conn);
			break;
		default:
			break;
		}
	}

	/**
	 * 插入简单重复事件。
	 * 
	 * @param conn
	 */
	private void insertSingleRepeatedEvents(Connection conn)
			throws SQLException {
	}

	/**
	 * 从实参变量中寻找指定名称的事件参数对象。
	 * 
	 * @param ref
	 * @return
	 */
	private Object getEventParamMapFromPointEvents(String ref) {
		for (int j = 0; j < pointEvents.size(); j++) {
			SIMEventParameter eventParameter = pointEvents.get(j);
			if (eventParameter.getDefinedID().equals(ref))
				return eventParameter.getEventParas();
		}
		return null;
	}

	/**
	 * 插入复杂重复事件。
	 * 
	 * @param conn
	 */
	private void insertComplexRepeatedEvents(Connection conn)
			throws SQLException, IllegalAccessException {
	}

	/**
	 * 当RuleDelegate初始化时，计算上次服务器后关闭前，该规则已经成功判断的重复事件数目。
	 * 
	 * @param flag
	 * @return
	 */
	private long countInitialRepeatedEvents(int flag) {
		return 0;
	}

	/**
	 * 根据规则要求的时间窗口，计算该窗口内，已经满足的重复事件数目
	 * 
	 * @param conn
	 * @param flag
	 */
	private long countRepeatedEvents(Connection conn, int flag) {
		switch (flag) {
		case org.opennms.netmgt.syslogd.common.Constants.SINGLE_REPEATED:
			return countSingleRepeatedEvents(conn);
		case org.opennms.netmgt.syslogd.common.Constants.GROUP_REPEATED:
			return countComplexRepeatedEvents(conn);
		case org.opennms.netmgt.syslogd.common.Constants.DISTINCT_REPEATED:
			return countComplexRepeatedEvents(conn);
		case org.opennms.netmgt.syslogd.common.Constants.COMPLEX_REPEATED:
			return countComplexRepeatedEvents(conn);
		default:
			break;
		}
		return 0;
	}

	/**
	 * 计算指定时间窗口内的简单重复事件的数目。
	 * 
	 * @param conn
	 * @return
	 */
	private long countSingleRepeatedEvents(Connection conn) {
		return 0;
	}

	/**
	 * 获得distinct的select的count语句
	 * 
	 * @return
	 */
	private String getCountDistinctSql() {
		if (hasElement(rule.getDiffVars())) {
			String sql = "";
			for (int i = 0; i < rule.getDiffVars().size() - 1; i++) {
				// sql += rule.getDiffVars().get(i).getAttrname() + ",";
				sql += getAttrForDB(rule.getDiffVars().get(i)) + ",";
			}
			// sql += rule.getDiffVars().get(rule.getDiffVars().size() - 1)
			// .getAttrname();
			// 20070809 获取列名
			sql += getAttrForDB(rule.getDiffVars().get(
					rule.getDiffVars().size() - 1));

			sql = "count(distinct " + sql + ")";
			return sql;
		} else
			return "count(*)";
	}

	/**
	 * 获得distinct的select的语句
	 * 
	 * @return
	 */
	private String getDistinctSql() {

		String sql = "";
		for (int i = 0; i < rule.getDiffVars().size() - 1; i++) {
			// sql += rule.getDiffVars().get(i).getAttrname() + ",";
			sql += getAttrForDB(rule.getDiffVars().get(i)) + ",";
		}
		// sql += rule.getDiffVars().get(rule.getDiffVars().size() - 1)
		// .getAttrname();
		// 20070809 获取列名
		sql += getAttrForDB(rule.getDiffVars().get(
				rule.getDiffVars().size() - 1));

		sql = "distinct " + sql + "";
		return sql;

	}

	/**
	 * 获得得到eventtrail时的group by 子句，他们由distinct的内容组成。
	 * 
	 * @return
	 */
	private String getGroupByDistinctSql() {
		if (hasElement(rule.getDiffVars())) {
			String sql = "";
			for (int i = 0; i < rule.getDiffVars().size() - 1; i++) {
				// sql += rule.getDiffVars().get(i).getAttrname() + ",";
				sql += getAttrForDB(rule.getDiffVars().get(i)) + ",";
			}
			// sql += rule.getDiffVars().get(rule.getDiffVars().size() - 1)
			// .getAttrname();
			sql += getAttrForDB(rule.getDiffVars().get(
					rule.getDiffVars().size() - 1));

			sql = " group by " + sql;
			return sql;
		} else
			return "";
	}

	/**
	 * 获得identical的语句
	 * 
	 * @return
	 */
	private String getIdenticalSql() throws IllegalAccessException {
		if (hasElement(rule.getIdVars())) {
			String sql = "";

			for (int i = 0; i < rule.getIdVars().size() - 1; i++) {
				VarBinds var = rule.getIdVars().get(i);
				String simpleAttr = var.getAttrname();
				String attr = getAttrForDB(var);
				Object event = getEventParamMapFromPointEvents(var.getRef());// 获得该idvar对应的实际参数。
				// Object value = eventMap.get(attr.toLowerCase());
				Object value = FilterAssistant.getSIMEventObjectAttribute(
						event, simpleAttr);
				if (value == null)
					sql += attr + " is null and ";
				else {
					if (value instanceof String)
						value = value.hashCode();
					sql += attr + "=" + value + " and ";
				}
			}

			VarBinds var = rule.getIdVars().get(rule.getIdVars().size() - 1);
			// String attr = var.getAttrname();
			String attr = getAttrForDB(var);
			String simpleAttr = var.getAttrname();
			Object event = getEventParamMapFromPointEvents(var.getRef());
			// Object value = eventMap.get(attr.toLowerCase());
			Object value = FilterAssistant.getSIMEventObjectAttribute(event,
					simpleAttr);
			if (value == null)
				sql += attr + " is null";
			else {
				if (value instanceof String)
					value = value.hashCode();
				sql += attr + "=" + value;
			}
			sql = " and " + sql;
			return sql;
		} else
			return "";
	}

	/**
	 * 计算指定时间窗口内的复杂重复事件的数目。
	 * 
	 * @param conn
	 * @return
	 */
	private long countComplexRepeatedEvents(Connection conn) {
		return 0;
	}

	/**
	 * 计算全部重复事件的数目
	 * 
	 * @param conn
	 */
	public long countAllRepeatedEvents(Connection conn) {
		return 0;
	}

	/**
	 * 获得select的event列名。比如"event1,event2"
	 * 
	 * @return
	 */
	private String getSelectEventIDSql() {
		String eventIDNameSql = "";
		for (int j = 0; j < pointEvents.size() - 1; j++) {
			SIMEventParameter eventParameter = pointEvents.get(j);
			String ref = eventParameter.getDefinedID();
			ref = ref.replace('-', '_');
			// ref = "f_" + ref;
			ref = "f_" + ref.split("_")[0] + "_" + Math.abs(ref.hashCode());// 取ref第一部分
			eventIDNameSql += ref + ",";
		}
		String lastRef = pointEvents.get(pointEvents.size() - 1).getDefinedID();
		lastRef = lastRef.replace('-', '_');
		// lastRef = "f_" + lastRef;
		lastRef = "f_" + lastRef.split("_")[0] + "_"
				+ Math.abs(lastRef.hashCode());// 取ref第一部分

		eventIDNameSql = eventIDNameSql + lastRef;
		return eventIDNameSql;
	}

	/**
	 * 获得select的event列名。比如"event1,event2"
	 * 
	 * @return
	 */
	private String getSelectEventIDSqlofMaxID() {
		String eventIDNameSql = "";
		for (int j = 0; j < pointEvents.size() - 1; j++) {
			SIMEventParameter eventParameter = pointEvents.get(j);
			String ref = eventParameter.getDefinedID();
			ref = ref.replace('-', '_');
			// ref = "f_" + ref;
			ref = "f_" + ref.split("_")[0] + "_" + Math.abs(ref.hashCode());// 取ref第一部分
			eventIDNameSql += "max(" + ref + "),";
		}
		String lastRef = pointEvents.get(pointEvents.size() - 1).getDefinedID();
		lastRef = lastRef.replace('-', '_');
		// lastRef = "f_" + lastRef;
		lastRef = "f_" + lastRef.split("_")[0] + "_"
				+ Math.abs(lastRef.hashCode());// 取ref第一部分

		eventIDNameSql = eventIDNameSql + "max(" + lastRef + ")";
		return eventIDNameSql;
	}

	/**
	 * 根据规则要求的时间窗口，计算该窗口内，已经满足的重复事件数目
	 * 
	 * @param conn
	 * @param flag
	 */
	private List fetchEventTrails(Connection conn, int flag)
			throws SQLException, IllegalAccessException {
		switch (flag) {
		case org.opennms.netmgt.syslogd.common.Constants.SINGLE_REPEATED:
			return fetchSingleEventTrails(conn);
		case org.opennms.netmgt.syslogd.common.Constants.GROUP_REPEATED:
			return fetchComplexEventTrails(conn);
		case org.opennms.netmgt.syslogd.common.Constants.DISTINCT_REPEATED:
			return fetchComplexEventTrails(conn);
		case org.opennms.netmgt.syslogd.common.Constants.COMPLEX_REPEATED:
			return fetchComplexEventTrails(conn);
		default:
			break;
		}
		return null;
	}

	/**
	 * 获得满足该规则的事件轨迹。这是一个动态函数。不是每次调用都有结果。只有在规则判断为满足时调用才有效。
	 * 
	 * @param conn
	 * @return
	 */
	private List fetchSingleEventTrails(Connection conn) throws SQLException,
			IllegalAccessException {
		return null;
	}

	/**
	 * 获得满足该规则的事件轨迹。这是一个动态函数。不是每次调用都有结果。只有在规则判断为满足时调用才有效。
	 * 
	 * @param conn
	 * @return
	 */
	private List fetchComplexEventTrails(Connection conn) throws SQLException,
			IllegalAccessException {
		return null;
	}

	/**
	 * 删除过时数据
	 * 
	 * @param conn
	 */
	public void removeOutDatedRepeatedEvents(Connection conn)
			throws SQLException {

	}

	/**
	 * 规则匹配成功后，删除那些已经匹配成功的事件。
	 * 
	 * @param conn
	 * @param flag
	 */
	private void removeRepeatedEvents(Connection conn, int flag)
			throws SQLException, IllegalAccessException {
		switch (flag) {
		case org.opennms.netmgt.syslogd.common.Constants.SINGLE_REPEATED:
			removeSingleRepeatedEvents(conn);
			break;
		case org.opennms.netmgt.syslogd.common.Constants.GROUP_REPEATED:
			removeComplexRepeatedEvents(conn);
			break;
		case org.opennms.netmgt.syslogd.common.Constants.DISTINCT_REPEATED:
			removeComplexRepeatedEvents(conn);
			break;
		case org.opennms.netmgt.syslogd.common.Constants.COMPLEX_REPEATED:
			removeComplexRepeatedEvents(conn);
			break;
		default:
			break;
		}
	}

	/**
	 * 删除简单重复事件。也就是清空整个表。
	 * 
	 * @param conn
	 */
	private void removeSingleRepeatedEvents(Connection conn)
			throws SQLException {

	}

	/**
	 * 删除简单重复事件。也就是清空整个表。
	 * 
	 * @param conn
	 */
	private void removeComplexRepeatedEvents(Connection conn)
			throws SQLException, IllegalAccessException {
	}

	/**
	 * 判断是否要进行重复事件判断，如果需要则进行重复事件判断
	 * 
	 * @return
	 */
	private boolean analyseRepeatedEvent() {
		TimeScope ts = rule.getTimeScope();
		if (ts != null && ts.getFrequency() == 1) {// 没有重复事件判断
			// 建立事件轨迹
			List<Long> trail = new ArrayList<Long>();

			for (int i = 0; i < pointEvents.size(); i++) {
				SIMEventParameter simp = pointEvents.get(i);
				trail.add((Long) simp.getEventID());
			}

			eventTrails = new ArrayList<List<Long>>();
			eventTrails.add(trail);

			pointEvents.clear();// 规则判断成功，则清空事件变量列表，为以后的重新判断做准备
			return true;
		}
		return false;
	}

	/**
	 * 规则判断的核心逻辑
	 * 
	 * @param currEventParams
	 *            当前传入的事件参数
	 * @return
	 */
	public boolean isSatisfiedInline(Object currEventParams) {

		// 对传入的告警事件进行判断。
		int type = (Integer) FilterAssistant.getSIMEventObjectAttribute(
				currEventParams, "systype");
		// logger.info("Rule:"+rule.getRuleID()+" Judge:"+type+"
		// "+currEventParams.getClass()+"
		// "+FilterAssistant.getSIMEventObjectAttribute(
		// currEventParams, "msg")+" "+(currEventParams instanceof
		// SIMAlertObject?((SIMAlertObject) currEventParams).originEventID:""));
		if (type == 2) {
			if (currEventParams instanceof SIMAlertObject) {
				if (hasAlreadyUsed(((SIMAlertObject) currEventParams).originEventID)
						|| ((String) FilterAssistant
								.getSIMEventObjectAttribute(currEventParams,
										"msg")).indexOf(rule.getRuleID()) != -1) {
					// 说明该事件为已经满足过本规则过滤器或者已经触发过本规则，为了避免重复告警和形成告警环路，造成无限告警的局面，在这里直接返回false，就不进行后面的判断了
					// logger.info("*****:"+(String)FilterAssistant
					// .getSIMEventObjectAttribute(currEventParams,
					// "msg")+"已经触发过该规则！！！-----");

					return false;
				}
			}
		}

		// -- 将当前传入的事件参数留作成员变量备用。--
		currentEvent = currEventParams;

		// -- 从当前事件参数中取出属性“接收时间”的值，后面的查询需要使用该值。--
		currentEventTime = FilterAssistant.getRecepttime(currEventParams);

		// -- 如果没有定义pointVars，则表示没有需要判断的过滤条件存在，直接返回false. --
		List<VarBinds> pointVars = rule.getPointVars();
		if (pointVars == null || pointVars.size() == 0)
			return false;

		// -- 依次查看每个point过滤器，找出一个满足条件的 --
		String satisfiedRef = judgeFromPointFilters(currEventParams);

		if (satisfiedRef.equals(""))// 没有满足基本事件过滤条件的，返回false.
			return false;
		else {
			// 如果成功的匹配了某过滤器，则将该事件id加入缓存表，供告警事件进入规则前进行潜在的重复告警判断。
			addfilterSatisfiedEvent((Long) FilterAssistant
					.getSIMEventObjectAttribute(currEventParams, "id"));
		}

		// -- 检验变量区 --
		if (pointVars.size() == 1) {// 单事件判断，没有关联分析
			return analyseRepeatedEvent();
		} else {// 检查所有point要求的事件变量是否同时满足需要
			// 组合所有pointEventHistoryMap中的事件，依次填入pointEvents，检查。
			List<String> filterIDList = new ArrayList<String>();// filterIDList与historyEventList元素一一对应。
			List<List> historyEventList = prepareHistoryEventList(
					currEventParams, satisfiedRef, filterIDList);

			if (historyEventList.size() != pointVars.size())// 说明没有足够的事件列表准备好
				return false;

			int[] hIndex = new int[historyEventList.size()];// 这里将采用类似的计数器进位的算法，来组合各种事件状态。
			// ---------------------------------------------------------------------
			do {
				// ---------------------------
				pointEvents.clear();
				for (int i = 0; i < historyEventList.size(); i++) {
					Object element = historyEventList.get(i).get(hIndex[i]);
					SIMEventParameter newParameter = new SIMEventParameter(
							filterIDList.get(i),
							FilterAssistant.getRecepttime(element),
							element instanceof Map ? FilterAssistant
									.cloneMap((Map) element) : element);
					pointEvents.add(newParameter);

				}

				// ---------------------------- 关联判断 --------------------
				boolean success = false;
				boolean bitOnce = false;
				if (associationFilter == null) {// 没有关联条件时，直接进行重复事件判断
					success = analyseRepeatedEvent();
				} else {
					if (bitOnce = associationFilter.isSatisfied(pointEvents)) {// 如果有关联条件，并且满足时，再进行重复事件的判断
						success = analyseRepeatedEvent();
					} else
						success = false;// 没有满足关联条件时，返回false
				}
				if (success || bitOnce) {// 对于这两种情况，都需要清除一次多事件匹配变量，因为它说明这些多事件变量已经使用过一次。
					for (int i = 0; i < historyEventList.size(); i++) {
						historyEventList.get(i).remove(hIndex[i]); // 系统的策略是事件对象只使用一次。
					}
					List satifiedList = pointEventHistoryMap.get(satisfiedRef)
							.getList();
					satifiedList.remove(satifiedList.size() - 1);// 去掉最后一个元素，就是本次匹配成功的那个事件
					// return true;// 成功返回
				}
				if (success)
					return true; // 成功返回
				if (bitOnce)// 说明success==false and bitOnce =
					// true;表示这是多事件，多重复的规则，关联关系成功但本次计数没有成功，等待下一次。
					return false;
				// ----------------------------

				hIndex[hIndex.length - 1]++;
				for (int i = hIndex.length - 1; i > 0; i--) {// 计数进位算法
					if (hIndex[i] != 0
							&& hIndex[i] >= historyEventList.get(i).size()) {
						hIndex[i] = 0;
						hIndex[i - 1]++;
					}
				}

			} while (hIndex[0] != historyEventList.get(0).size());

			return false;// 没有组合满足要求
		}

	}

	/**
	 * 将传入的事件对象参数，依次经过基本过滤器的判断，返回满足某过滤器的过滤器的id；，没有满足的过滤器则返回""
	 * 
	 * @param currEventParams
	 * @return
	 */
	private String judgeFromPointFilters(Object currEventParams) {
		String satisfiedRef = "";
		List<VarBinds> pointVars = rule.getPointVars();
		for (int i = 0; i < pointVars.size(); i++) {
			VarBinds var = pointVars.get(i);
			FilterDelegate delegate = pointFilterDelegateMaps.get(var.getRef());
			if (delegate.isSatisfied(currEventParams)) {// 实际的满足性判断。
				satisfiedRef = var.getRef();
				// -- 记录该事件对象为成功匹配的事件对象，并存于对应的历史记录中。new --
				if (pointVars.size() > 1)// 对事件关联时才需要事件状态存取
					pointEventHistoryMap.get(satisfiedRef).accept(
							currEventParams);
				else {
					// -- 更新变量。目前下面的代码为单事件判断做准备。 --
					SIMEventParameter newParameter = new SIMEventParameter(
							satisfiedRef,
							FilterAssistant.getRecepttime(currEventParams),
							currEventParams instanceof Map ? FilterAssistant
									.cloneMap((Map) currEventParams)
									: currEventParams);
					int j = 0;
					for (; j < pointEvents.size(); j++) {
						SIMEventParameter eventParameter = pointEvents.get(j);
						if (eventParameter != null
								&& eventParameter.getDefinedID().equals(
										satisfiedRef)) {
							pointEvents.set(j, newParameter);
							break;
						}
					}
					if (j == pointEvents.size()) {
						pointEvents.add(newParameter);
					}

				}
				break;
			}
		}
		return satisfiedRef;
	}

	/**
	 * 准备各个事列表，其中包含去除超时事件的过程。filterIDList将返回个列表对应的filterid
	 * 
	 * @param currEventParams
	 *            此次被判断的事件
	 * @param satisfiedRef
	 *            满足的基本过滤器的id号
	 * @param filterIDList
	 * @return
	 */
	private List<List> prepareHistoryEventList(Object currEventParams,
			String satisfiedRef, List<String> filterIDList) {
		List<List> historyEventList = new ArrayList<List>();
		List<VarBinds> pointVars = rule.getPointVars();
		for (int i = 0; i < pointVars.size(); i++) {
			String ref = pointVars.get(i).getRef();
			if (!satisfiedRef.equals(ref)) {// 对于非本次事件满足的事件列表来说

				EventStatusList esl = pointEventHistoryMap.get(ref);// 取得事件历史列表
				if (esl != null) {
					List hList = esl.getList();
					if ((FilterAssistant.getRecepttime(currEventParams) - esl
							.getMinTime()) > windowTimeSize) {// 时间窗口判断该队列中有事件超时
						for (int j = 0; j < hList.size(); j++) {
							Object event = hList.get(j);
							if ((FilterAssistant.getRecepttime(currEventParams) - FilterAssistant
									.getRecepttime(event)) > windowTimeSize) {// 寻找超时对象
								hList.remove(j);// 去掉超时对象
								j--;
							}
						}
					}
					if (hList.size() <= 0) {
						// 该列中没有满足要求的事件对象，则无法进行多事件关联。
						return historyEventList;
					}
					historyEventList.add(hList);
					filterIDList.add(esl.getFilterID());
					// System.out.println(satisfiedRef+" "+ref+"
					// "+historyEventList.size()+" "+hList.get(0)+"
					// "+currEventParams);
				} else
					return historyEventList;

			}
		}
		List satisfiedEventList = new ArrayList();
		satisfiedEventList.add(currEventParams);
		historyEventList.add(satisfiedEventList);// 注意此两行：为了调用他的函数使用。表示该currEventParams也需要在列表中。
		filterIDList.add(satisfiedRef);

		// 至此已经准备好进行组合筛选的事件对象列表，该列表的每一个元素也是一个列表，对应时间窗口内满足相关过滤器的事件列表

		return historyEventList;
	}

	/**
	 * 获得满足当前规则的事件轨迹。
	 * 
	 * @return 其中的每个元素是事件id
	 */
	public List getEventTrails() {
		return eventTrails;
	}

	private void displayEventTrails() {
		if (eventTrails == null) {
			System.out.println("No trails now");
		} else {
			System.out.println("Trails:");
			for (int i = 0; i < eventTrails.size(); i++) {
				List row = eventTrails.get(i);
				if (row != null) {
					for (int j = 0; j < row.size(); j++) {
						System.out.print(row.get(j) + ", ");
					}
					System.out.println();
				}
			}
		}
	}

	/**
	 * 向成功匹配过滤器的数组中添加事件ID号。
	 * 
	 * @param id
	 */
	private void addfilterSatisfiedEvent(long id) {
		satisfiedIDIndex = (satisfiedIDIndex++)
				% CACHED_FILTER_SATISFIEDARRAY_LENGTH;
		filterSatisfiedArray[satisfiedIDIndex] = id;
	}

	/**
	 * 判断是否是已经使用过的满足过滤器的事件id
	 * 
	 * @param eventID
	 * @return
	 */
	private boolean hasAlreadyUsed(long eventID) {
		for (int i = 0; i < CACHED_FILTER_SATISFIEDARRAY_LENGTH; i++) {
			if (eventID == filterSatisfiedArray[i])
				return true;
		}
		return false;
	}

	public Object getCurrentEvent() {
		return currentEvent;
	}

	public void setCurrentEvent(Object currentEvent) {
		this.currentEvent = currentEvent;
	}

}

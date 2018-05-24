package org.opennms.netmgt.syslogd.analyze;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.syslogd.logic.BetweenLogic;
import org.opennms.netmgt.syslogd.logic.ContainsICLogic;
import org.opennms.netmgt.syslogd.logic.ContainsLogic;
import org.opennms.netmgt.syslogd.logic.EQLogic;
import org.opennms.netmgt.syslogd.logic.EQStrICLogic;
import org.opennms.netmgt.syslogd.logic.EQStrLogic;
import org.opennms.netmgt.syslogd.logic.EndswithICLogic;
import org.opennms.netmgt.syslogd.logic.EndswithLogic;
import org.opennms.netmgt.syslogd.logic.GELogic;
import org.opennms.netmgt.syslogd.logic.GTLogic;
import org.opennms.netmgt.syslogd.logic.InICLogic;
import org.opennms.netmgt.syslogd.logic.InLogic;
import org.opennms.netmgt.syslogd.logic.IncategoryLogic;
import org.opennms.netmgt.syslogd.logic.InnumberLogic;
import org.opennms.netmgt.syslogd.logic.InsubnetLogic;
import org.opennms.netmgt.syslogd.logic.IsnullLogic;
import org.opennms.netmgt.syslogd.logic.LELogic;
import org.opennms.netmgt.syslogd.logic.LTLogic;
import org.opennms.netmgt.syslogd.logic.LogicUnit;
import org.opennms.netmgt.syslogd.logic.MatchesAddrResource;
import org.opennms.netmgt.syslogd.logic.MatchesBWListLogic;
import org.opennms.netmgt.syslogd.logic.MatchesFilterLogic;
import org.opennms.netmgt.syslogd.logic.MatchesFilterPluginLogic;
import org.opennms.netmgt.syslogd.logic.MatchesICLogic;
import org.opennms.netmgt.syslogd.logic.MatchesLogic;
import org.opennms.netmgt.syslogd.logic.MatchesPortResourceLogic;
import org.opennms.netmgt.syslogd.logic.MatchesTimeResourceLogic;
import org.opennms.netmgt.syslogd.logic.NELogic;
import org.opennms.netmgt.syslogd.logic.NEStrLogic;
import org.opennms.netmgt.syslogd.logic.StartswithICLogic;
import org.opennms.netmgt.syslogd.logic.StartswithLogic;
import org.opennms.netmgt.syslogd.parse.SIMEventObject;

public class FilterAssistant {
	/**
	 * 日志接口
	 */

	/**
	 * 事件对像的成员属性数组
	 */
	private static Field[] fieldsforSIMEvent = SIMEventObject.class
			.getDeclaredFields();

	/**
	 * 告警对像的成员属性数组
	 */
	private static Field[] fieldsforSIMAlert = SIMAlertObject.class.getFields();

	/**
	 * 注册的操作运算符。使用map方式，可以方便扩充新的运算和提高选择指定运算符的速度。
	 */
	private static Map<String, LogicUnit> oprs = new HashMap<String, LogicUnit>();

	static {
		oprs.put(Constants.OPR_BETWEEN, new BetweenLogic());
		oprs.put(Constants.OPR_CONTAINS_IC, new ContainsICLogic());
		oprs.put(Constants.OPR_CONTAINS, new ContainsLogic());
		oprs.put(Constants.OPR_ENDSWITH_IC, new EndswithICLogic());
		oprs.put(Constants.OPR_ENDSWITH, new EndswithLogic());
		oprs.put(Constants.OPR_EQ, new EQLogic());
		oprs.put(Constants.OPR_EQSTR_IC, new EQStrICLogic());
		oprs.put(Constants.OPR_EQSTR, new EQStrLogic());
		oprs.put(Constants.OPR_NESTR, new NEStrLogic());
		oprs.put(Constants.OPR_GE, new GELogic());
		oprs.put(Constants.OPR_GT, new GTLogic());
		oprs.put(Constants.OPR_INCATEGORY, new IncategoryLogic());
		oprs.put(Constants.OPR_IN_IC, new InICLogic());
		oprs.put(Constants.OPR_IN, new InLogic());
		oprs.put(Constants.OPR_INNUMBER, new InnumberLogic());
		oprs.put(Constants.OPR_INSUBNET, new InsubnetLogic());
		oprs.put(Constants.OPR_ISNULL, new IsnullLogic());
		oprs.put(Constants.OPR_LE, new LELogic());
		oprs.put(Constants.OPR_LT, new LTLogic());
		oprs.put(Constants.OPR_MATCHESFILTER, new MatchesFilterLogic());
		oprs.put(Constants.OPR_MATCHES_IC, new MatchesICLogic());
		oprs.put(Constants.OPR_MATCHES, new MatchesLogic());
		oprs.put(Constants.OPR_NE, new NELogic());
		oprs.put(Constants.OPR_STARTSWITH_IC, new StartswithICLogic());
		oprs.put(Constants.OPR_STARTSWITH, new StartswithLogic());

		oprs.put(Constants.OPR_MATCHES_ADDR_RESOURCE, new MatchesAddrResource());
		oprs.put(Constants.OPR_MATCHES_PORT_RESOURCE,
				new MatchesPortResourceLogic());
		oprs.put(Constants.OPR_MATCHES_TIME_RESOURCE,
				new MatchesTimeResourceLogic());
		oprs.put(Constants.OPR_MATCHESFILTERPLUGIN,
				new MatchesFilterPluginLogic());
		oprs.put(Constants.OPR_MATCHESBWLIST, new MatchesBWListLogic());

	}

	/**
	 * 从事件参数中获取属性的值，事件参数可能是SIMEventObject类型，也可能是Map类型
	 * 
	 * @param eventParameters
	 * @param attrName
	 * @return
	 */
	public static Object getSIMEventObjectAttribute(Object eventParameters,
			String attrName) {
		if (eventParameters == null)
			return null;
		if (eventParameters instanceof SIMEventObject) {
			return getSIMEventObjectAttribute((SIMEventObject) eventParameters,
					attrName);
		} else if (eventParameters instanceof Map) {
			return ((Map) eventParameters).get(attrName);
		} else
			return null;
	}

	/**
	 * 取得类的属性类型.函数不区分attr的大小写，找到第一个满足的属性成员即返回其类型。
	 * 
	 * @param destClass
	 * @param attrName
	 * @return
	 */
	public static String getAttrType(Class destClass, String attrName) {
		String type = "";
		Field[] f = destClass.getDeclaredFields();
		if (f != null) {
			for (int i = 0; i < f.length; i++) {
				Field field = f[i];
				if (field.getName().equalsIgnoreCase(attrName)) {
					type = field.getType().getName();
					break;
				}
			}
		}
		return type;
	}

	/**
	 * 设置事件类成员属性的值。
	 * 
	 * @param attrName
	 * @param attrValue
	 * @param alert
	 */
	public static void setAlertAttr(String attrName, String attrValue,
			SIMEventObject alert) {

		// Class destClass = SIMEventObject.class;
		// Field[] f = destClass.getDeclaredFields();
		// if (f != null) {
		String type = "";
		for (int i = 0; i < fieldsforSIMEvent.length; i++) {
			Field field = fieldsforSIMEvent[i];
			if (field.getName().equalsIgnoreCase(attrName)) {
				if (attrValue != null)
					attrValue = attrValue.trim();
				try {
					type = field.getType().getName();
					if (type.equalsIgnoreCase("java.lang.String"))
						field.set(alert, attrValue);
					else if (type.equalsIgnoreCase("long")
							|| type.equalsIgnoreCase("java.lang.Long"))
						field.set(alert, new Long(Long.parseLong(attrValue)));
					else if (type.equalsIgnoreCase("int")
							|| type.equalsIgnoreCase("java.lang.Integer"))
						field.set(alert,
								new Integer(Integer.parseInt(attrValue)));
					else if (type.equalsIgnoreCase("byte")
							|| type.equalsIgnoreCase("java.lang.Byte"))
						field.set(alert, new Byte(Byte.parseByte(attrValue)));
					else if (type.equalsIgnoreCase("short")
							|| type.equalsIgnoreCase("java.lang.Short"))
						field.set(alert, new Short(Short.parseShort(attrValue)));
					else if (type.equalsIgnoreCase("double")
							|| type.equalsIgnoreCase("java.lang.Double"))
						field.set(alert,
								new Double(Double.parseDouble(attrValue)));
					else if (type.equalsIgnoreCase("float")
							|| type.equalsIgnoreCase("java.lang.Float"))
						field.set(alert, new Float(Float.parseFloat(attrValue)));
					else if (type.equalsIgnoreCase("boolean")
							|| type.equalsIgnoreCase("java.lang.Boolean"))
						field.set(alert,
								new Boolean(Boolean.parseBoolean(attrValue)));
					else if (type.equalsIgnoreCase("char")
							|| type.equalsIgnoreCase("java.lang.Character"))
						field.set(alert,
								attrValue.length() > 0 ? attrValue.charAt(0)
										: 0);

				} catch (Exception e) {

				}
				// }
				// field.s
				break;
			}
		}
		// }
	}

	/**
	 * 获得与事件类型相关的参数值
	 * 
	 * @param var
	 * @param parameters
	 * @return
	 */
	public static Object getEventOperand(VarBinds var, Object parameters)
			throws IllegalAccessException {

		String attrName = var.getAttrname();
		if (!StringTools.hasContent(attrName) || parameters == null)// 判断属性或参数是否为空
			return null;

		if (parameters instanceof SIMEventObject) {// 单事件参数
			return getSIMEventObjectAttribute((SIMEventObject) parameters,
					attrName);
		} else if (parameters instanceof Map) {// 单事件参数
			return ((Map) parameters).get(attrName);
		} else {// 多事件参数

			Object realEventParameter = null;
			String ref = var.getRef();

			if (StringTools.hasContent(ref) && parameters instanceof List) {
				List<SIMEventParameter> events = (List) parameters;
				for (int i = 0; i < events.size(); i++) {// 查找指定参数事件
					SIMEventParameter eP = events.get(i);
					if (eP != null && eP.getDefinedID().equals(ref)) {// 比较var中要求的ref是不是本事件
						realEventParameter = eP.getEventParas();
						break;
					}
				}
			}

			if (realEventParameter == null)// 如果没有参数表，则返回null
				return null;
			if (realEventParameter instanceof SIMEventObject) {
				return getSIMEventObjectAttribute(
						(SIMEventObject) realEventParameter, attrName);
			} else if (realEventParameter instanceof Map)
				return ((Map) realEventParameter).get(attrName);
			else
				return null;

		}

	}

	/**
	 * 获得逻辑运算的左操作数。
	 * 
	 * @param var
	 * @param parameters
	 * @return
	 */
	public static Object getLeftOperand(VarBinds var, Object parameters)
			throws IllegalAccessException {

		if (var == null)
			return null;

		if (var.getType().equals(Constants.VAR_EVENT)) {// 事件变量

			return getEventOperand(var, parameters);// 动态类型，决定于组装varMap时的对象。

		} else if (var.getType().equals(Constants.VAR_FILTER)) {// 过滤器

			return FilterManagerFactory.getFilterManager().getFilterDelegate(
					var.getRef());// FilterDelegate

		} else if (var.getType().equals(Constants.VAR_FILTERPLUGIN)) {// 过滤器插件

			return FilterPluginConfig.getFilterPluginConfig()
					.getFilterPluginByID(var.getRef());// FilterPlugin

		} else if (var.getType().equals(Constants.VAR_BWLIST)) {// 黑白名单,本质上也是一种过滤器插件

			return FilterPluginConfig.getFilterPluginConfig()
					.getFilterPluginByID(Constants.BWListID);// FilterPlugin

		} else if (var.getType().equals(Constants.VAR_CONSTANT)) {// 常量，单值常量,左操作数只允许出现单值常量

			return var.getValue();// String

		} else
			return null;

	}

	/**
	 * 获得逻辑运算的右操作数。 Modify by liyue 2013-1-28
	 * 增加needAddressTrans参数，作为左侧符号为地址类字段，需要进行地址转换以支持IPv6
	 * 
	 * @param var
	 * @param opr
	 * @param parameters
	 * @return
	 */
	public static Object getRightOperand(VarBinds var, String opr,
			Object parameters, boolean needAddressTrans)
			throws IllegalAccessException {
		if (var == null)
			return null;

		if (var.getType().equals(Constants.VAR_CONSTANT)) {// 常量
			if (Constants.nonSingleOprands.contains(opr))// 非单值常量
			{
				if (needAddressTrans) {
					String[] inParams = (String[]) var.getValues();
					for (int i = 0; i < inParams.length; i++) {
						try {
							inParams[i] = InetAddress.getByName(inParams[i])
									.getHostAddress();
						} catch (UnknownHostException e) {
							continue;
						}
					}
					return inParams;
				} else
					return var.getValues();// String[]
			} else {
				if (needAddressTrans) {
					try {
						return InetAddress.getByName(var.getValue())
								.getHostAddress();
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						return var.getValue();
					}
				}
				return var.getValue();// String
			}
		} else if (var.getType().equals(Constants.VAR_EVENT)) {// 事件变量

			return getEventOperand(var, parameters);

		} else if (var.getType().equals(Constants.VAR_FILTER)) {// 过滤器类型，返回当前事件属性。

			return parameters;

		} else if (var.getType().equals(Constants.VAR_RESOURCE)) {// 资源引用类型，返回当前ref中存储的资源ID。

			return var.getRef();

		} else if (var.getType().equals(Constants.VAR_INTERN)) {// 内部对象类型，返回当前internObject。目前insubnet操作符的右操作数为此类型

			return var.getInternValueObject();

		} else if (var.getType().equals(Constants.VAR_FILTERPLUGIN)) {// 过滤器插件类型，返回新的列表，第一个于是原属是当前事件属性，第二个元素是var的values数组。
			List arguments = new ArrayList();
			arguments.add(parameters);
			arguments.add(var.getValues());
			return arguments;

		} else if (var.getType().equals(Constants.VAR_BWLIST)) {// 黑白名单类型，返回新的列表，第一个于是原属是当前事件属性，第二个元素是String数组，数组中存放指定黑白名单id的引用。
			List arguments = new ArrayList();
			String[] bwlistID = new String[1];
			bwlistID[0] = var.getRef();
			arguments.add(parameters);
			arguments.add(bwlistID);
			return arguments;

		} else
			return null;

	}

	/**
	 * 从参数中获得事件接收时间,如果不能获得，则返回系统当前时间
	 * 
	 * @param params
	 * @return
	 */
	public static long getRecepttime(Object params) {
		try {
			if (params != null) {
				if (params instanceof SIMEventObject) {
					return ((SIMEventObject) params).getReceptTime();
				} else {
					return (Long) ((Map) params).get(Constants.TimeStampName);
				}
			}
		} catch (Exception e) {

		}
		return System.currentTimeMillis();
	}

	/**
	 * 计算逻辑表达式
	 * 
	 * @param left
	 * @param opr
	 * @param right
	 * @return
	 */
	public static boolean cal(Object left, String opr, Object right) {
		LogicUnit lu = oprs.get(opr);
		if (lu == null)
			throw new IllegalArgumentException("undefined opr:" + opr);
		return lu.judge(left, right);
	}

	/**
	 * 将map复制到新的map中。
	 * 
	 * @param src
	 * @return
	 */
	public static Map cloneMap(Map src) {
		if (src == null)
			return null;
		Map newMap = new HashMap();
		Set ks = src.keySet();
		for (Iterator iter = ks.iterator(); iter.hasNext();) {
			Object key = iter.next();
			Object value = src.get(key);
			newMap.put(key, value);
		}
		return newMap;
	}

	/**
	 * 每次修改事件属性后，都要重新运行该段代码。
	 * 
	 */
	public static void generateSourceCodeForSIMEventObject() {
		String blank1 = "	";
		String blank2 = "		";
		String blank3 = "			";
		String rn = "\n";

		StringBuffer sourceCode = new StringBuffer();
		sourceCode
				.append(blank1
						+ "//-----------------以下代码通过printSourceCodeForGetAttribute()自动生成----------------------------------------"
						+ rn);
		sourceCode.append(blank1 + "/**" + rn);
		sourceCode.append(blank1 + " * SIMEventObject属性名称的小写与属性索引对照表" + rn);
		sourceCode.append(blank1 + " */" + rn);
		sourceCode.append(blank1
				+ "public static Map fieldNameIndexMap = new HashMap();" + rn);
		sourceCode.append(blank1 + "static {" + rn);
		for (int i = 0; i < fieldsforSIMEvent.length; i++) {
			String fieldName = fieldsforSIMEvent[i].getName().toLowerCase();
			sourceCode.append(blank2 + "fieldNameIndexMap.put(\"" + fieldName
					+ "\", " + i + ");" + rn);
		}
		sourceCode.append(blank1 + "}" + rn);

		sourceCode.append(blank1 + "/**" + rn);
		sourceCode.append(blank1 + " * 根据属性名称（均为小写），获得事件对象对应的属性值。 " + rn);
		sourceCode.append(blank1 + " * @param event" + rn);
		sourceCode.append(blank1 + " * @param attrName" + rn);
		sourceCode.append(blank1 + " * @return" + rn);
		sourceCode.append(blank1 + " */" + rn);
		sourceCode
				.append(blank1
						+ "public static Object getSIMEventObjectAttribute(SIMEventObject event,	String attrName) {"
						+ rn);
		sourceCode.append(blank1 + "" + rn);
		sourceCode.append(blank2
				+ "int index = (Integer)fieldNameIndexMap.get(attrName);" + rn);
		sourceCode.append(blank2 + "switch(index){" + rn);

		for (int i = 0; i < fieldsforSIMEvent.length; i++) {
			String fieldName = fieldsforSIMEvent[i].getName();
			sourceCode.append(blank3 + "case " + i + ": return event."
					+ fieldName + ";" + rn);
		}
		sourceCode.append(blank3 + "default: return null;" + rn);
		sourceCode.append(blank2 + "}" + rn);
		sourceCode.append(blank1 + "}" + rn);
		sourceCode.append(blank1 + "" + rn);

		sourceCode.append(blank1 + "/**" + rn);
		sourceCode.append(blank1 + " * 根据告警事件对像，准备需要需要执行告警动作的参数表。 *" + rn);
		sourceCode.append(blank1 + " *" + rn);
		sourceCode.append(blank1 + " * @param alert" + rn);
		sourceCode.append(blank1 + " * @return" + rn);
		sourceCode.append(blank1 + " */" + rn);

		sourceCode
				.append(blank1
						+ "public static String[][] prepareParametersForAction(SIMAlertObject alert) {"
						+ rn);
		sourceCode
				.append(blank2
						+ "String[][] params = new String[fieldsforSIMAlert.length][2];"
						+ rn);
		sourceCode.append(blank1 + "" + rn);
		for (int i = 0; i < fieldsforSIMAlert.length; i++) {
			String fieldName = fieldsforSIMAlert[i].getName();
			sourceCode.append(blank2 + "params[" + i + "][0] = \""
					+ fieldName.toLowerCase() + "\";" + rn);
			sourceCode.append(blank2 + "params[" + i
					+ "][1] = String.valueOf(alert." + fieldName + ");" + rn);
			sourceCode.append(blank1 + "" + rn);
		}
		sourceCode.append(blank2 + "return params;" + rn);
		sourceCode.append(blank1 + "}" + rn);

		sourceCode.append(blank1 + "/**" + rn);
		sourceCode.append(blank1 + " * 根据事件对像，拷贝到新的告警对象中。 " + rn);
		sourceCode.append(blank1 + " *" + rn);
		sourceCode.append(blank1 + " * @param src" + rn);
		sourceCode.append(blank1 + " * @return" + rn);
		sourceCode.append(blank1 + " */" + rn);

		sourceCode.append(blank1
				+ "public static SIMAlertObject copy(SIMEventObject src) {"
				+ rn);
		sourceCode.append(blank2 + "if (src == null)" + rn);
		sourceCode.append(blank3 + "return null;" + rn);
		sourceCode.append(blank2
				+ "SIMAlertObject alert = new SIMAlertObject();" + rn);

		for (int i = 0; i < fieldsforSIMEvent.length; i++) {
			String fieldName = fieldsforSIMEvent[i].getName();
			sourceCode.append(blank2 + "alert." + fieldName + "  = src."
					+ fieldName + ";" + rn);
		}
		sourceCode.append(blank1 + "" + rn);
		sourceCode.append(blank2 + "return alert;" + rn);
		sourceCode.append(blank1 + "}" + rn);

		sourceCode
				.append(blank1
						+ "//-----------------自动生成代码结束-----------------------------------------"
						+ rn);
		System.out.println(sourceCode);
	}

	/**
	 * SIMEventObject属性名称的小写与属性索引对照表 2012-8-29 liyue 针对新版事件属性字段进行修改
	 */
	public static Map fieldNameIndexMap = new HashMap();

	static {
		fieldNameIndexMap.put("id", 0);
		fieldNameIndexMap.put("recepttime", 1);
		fieldNameIndexMap.put("aggregatedcount", 2);
		fieldNameIndexMap.put("systype", 3);
		fieldNameIndexMap.put("collectoraddr", 4);
		fieldNameIndexMap.put("collectorname", 5);
		fieldNameIndexMap.put("collecttype", 6);
		fieldNameIndexMap.put("name", 7);
		fieldNameIndexMap.put("category", 8);
		fieldNameIndexMap.put("type", 9);
		fieldNameIndexMap.put("priority", 10);
		fieldNameIndexMap.put("rawid", 11);
		fieldNameIndexMap.put("occurtime", 12);
		fieldNameIndexMap.put("duration", 13);
		fieldNameIndexMap.put("send", 14);
		fieldNameIndexMap.put("receive", 15);
		fieldNameIndexMap.put("protocol", 16);
		fieldNameIndexMap.put("appprotocol", 17);
		fieldNameIndexMap.put("object", 18);
		fieldNameIndexMap.put("policy", 19);
		fieldNameIndexMap.put("resource", 20);
		fieldNameIndexMap.put("action", 21);
		fieldNameIndexMap.put("intent", 22);
		fieldNameIndexMap.put("result", 23);
		fieldNameIndexMap.put("saddr", 24);
		fieldNameIndexMap.put("sport", 25);
		fieldNameIndexMap.put("susername", 26);
		fieldNameIndexMap.put("staddr", 27);
		fieldNameIndexMap.put("stport", 28);
		fieldNameIndexMap.put("daddr", 29);
		fieldNameIndexMap.put("dport", 30);
		fieldNameIndexMap.put("dusername", 31);
		fieldNameIndexMap.put("dtaddr", 32);
		fieldNameIndexMap.put("dtport", 33);
		fieldNameIndexMap.put("devaddr", 34);
		fieldNameIndexMap.put("devname", 35);
		fieldNameIndexMap.put("devcategory", 36);
		fieldNameIndexMap.put("devtype", 37);
		fieldNameIndexMap.put("devvendor", 38);
		fieldNameIndexMap.put("devproduct", 39);
		fieldNameIndexMap.put("programtype", 40);
		fieldNameIndexMap.put("sessionid", 41);
		fieldNameIndexMap.put("requesturi", 42);
		fieldNameIndexMap.put("msg", 53);
		fieldNameIndexMap.put("customs1", 43);
		fieldNameIndexMap.put("customs2", 44);
		fieldNameIndexMap.put("customs3", 45);
		fieldNameIndexMap.put("customs4", 46);
		fieldNameIndexMap.put("smac", 47);
		fieldNameIndexMap.put("dmac", 48);
		fieldNameIndexMap.put("customd1", 49);
		fieldNameIndexMap.put("customd2", 50);
		fieldNameIndexMap.put("customd3", 51);
		fieldNameIndexMap.put("customd4", 52);
	}

	/**
	 * 根据属性名称（均为小写），获得事件对象对应的属性值。
	 * 
	 * @param event
	 * @param attrName
	 * @return
	 */
	public static Object getSIMEventObjectAttribute(SIMEventObject event,
			String attrName) {

		int index = (Integer) fieldNameIndexMap.get(attrName);
		switch (index) {
		case 0:
			return event.ID;
		case 1:
			return event.receptTime;
		case 2:
			return event.aggregatedCount;
		case 3:
			return event.sysType;
		case 4:
			try {
				return InetAddress.getByName(event.collectorAddr)
						.getHostAddress();
			} catch (UnknownHostException e) {
				return event.collectorAddr;
			}
		case 5:
			return event.collectorName;
		case 6:
			return event.collectType;
		case 7:
			return event.name;
		case 8:
			return event.category;
		case 9:
			return event.type;
		case 10:
			return event.priority;
		case 11:
			return event.rawID;
		case 12:
			return event.occurTime;
		case 13:
			return event.duration;
		case 14:
			return event.send;
		case 15:
			return event.receive;
		case 16:
			return event.protocol;
		case 17:
			return event.appProtocol;
		case 18:
			return event.object;
		case 19:
			return event.policy;
		case 20:
			return event.resource;
		case 21:
			return event.action;
		case 22:
			return event.intent;
		case 23:
			return event.result;
		case 24:
			try {
				return InetAddress.getByName(event.sAddr).getHostAddress();
			} catch (UnknownHostException e) {
				return event.sAddr;
			}
		case 25:
			return event.sPort;
		case 26:
			return event.sUserName;
		case 27:
			try {
				return InetAddress.getByName(event.stAddr).getHostAddress();
			} catch (UnknownHostException e) {
				return event.stAddr;
			}
		case 28:
			return event.stPort;
		case 29:
			try {
				return InetAddress.getByName(event.dAddr).getHostAddress();
			} catch (UnknownHostException e) {
				return event.dAddr;
			}
		case 30:
			return event.dPort;
		case 31:
			return event.dUserName;
		case 32:
			try {
				return InetAddress.getByName(event.dtAddr).getHostAddress();
			} catch (UnknownHostException e) {
				return event.dtAddr;
			}
		case 33:
			return event.dtPort;
		case 34:
			try {
				return InetAddress.getByName(event.devAddr).getHostAddress();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				return event.devAddr;
			}
		case 35:
			return event.devName;
		case 36:
			return event.devCategory;
		case 37:
			return event.devType;
		case 38:
			return event.devVendor;
		case 39:
			return event.devProduct;
		case 40:
			return event.programType;
		case 41:
			return event.sessionID;
		case 42:
			return event.requestURI;
		case 53:
			return event.msg;
		case 43:
			return event.customS1;
		case 44:
			return event.customS2;
		case 45:
			return event.customS3;
		case 46:
			return event.customS4;
		case 47:
			return event.sMAC;
		case 48:
			return event.dMAC;
		case 49:
			return event.customD1;
		case 50:
			return event.customD2;
		case 51:
			return event.customD3;
		case 52:
			return event.customD4;
		default:
			return null;
		}
	}

}

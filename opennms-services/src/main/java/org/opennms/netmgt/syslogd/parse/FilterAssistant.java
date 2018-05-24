package org.opennms.netmgt.syslogd.parse;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.syslogd.logic.GELogic;
import org.opennms.netmgt.syslogd.logic.GTLogic;
import org.opennms.netmgt.syslogd.logic.InICLogic;
import org.opennms.netmgt.syslogd.logic.InLogic;
import org.opennms.netmgt.syslogd.logic.InnumberLogic;
import org.opennms.netmgt.syslogd.logic.IsnullLogic;
import org.opennms.netmgt.syslogd.logic.LELogic;
import org.opennms.netmgt.syslogd.logic.LTLogic;
import org.opennms.netmgt.syslogd.logic.LogicUnit;
import org.opennms.netmgt.syslogd.logic.NELogic;
import org.opennms.netmgt.syslogd.logic.NEStrLogic;
import org.opennms.netmgt.syslogd.logic.StartswithICLogic;
import org.opennms.netmgt.syslogd.logic.StartswithLogic;

public class FilterAssistant {
	private static Field[] fieldsforSIMEvent = SIMEventObject.class
			.getDeclaredFields();
	private static Map<String, LogicUnit> oprs = new HashMap<String, LogicUnit>();
	static {
		oprs.put(Constants.OPR_NESTR, new NEStrLogic());
		oprs.put(Constants.OPR_GE, new GELogic());
		oprs.put(Constants.OPR_GT, new GTLogic());
		oprs.put(Constants.OPR_IN_IC, new InICLogic());
		oprs.put(Constants.OPR_IN, new InLogic());
		oprs.put(Constants.OPR_INNUMBER, new InnumberLogic());
		oprs.put(Constants.OPR_ISNULL, new IsnullLogic());
		oprs.put(Constants.OPR_LE, new LELogic());
		oprs.put(Constants.OPR_LT, new LTLogic());
		oprs.put(Constants.OPR_NE, new NELogic());
		oprs.put(Constants.OPR_STARTSWITH_IC, new StartswithICLogic());
		oprs.put(Constants.OPR_STARTSWITH, new StartswithLogic());
	}

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

	public static Object getEventOperand(VarBinds var, Object parameters)
			throws IllegalAccessException {

		String attrName = var.getAttrname();
		if (parameters == null)//
			return null;

		if (parameters instanceof SIMEventObject) {//
			return getSIMEventObjectAttribute((SIMEventObject) parameters,
					attrName);
		} else if (parameters instanceof Map) {//
			return ((Map) parameters).get(attrName);
		} else {//

			Object realEventParameter = null;
			String ref = var.getRef();

			if (parameters instanceof List) {
				List<SIMEventParameter> events = (List) parameters;
				for (int i = 0; i < events.size(); i++) {//
					SIMEventParameter eP = events.get(i);
					if (eP != null && eP.getDefinedID().equals(ref)) {//
						realEventParameter = eP.getEventParas();
						break;
					}
				}
			}

			if (realEventParameter == null)//
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

	public static Object getLeftOperand(VarBinds var, Object parameters)
			throws IllegalAccessException {

		if (var == null)
			return null;

		if (var.getType().equals(Constants.VAR_EVENT)) {//

			return getEventOperand(var, parameters);//

		} else if (var.getType().equals(Constants.VAR_CONSTANT)) {// ��������ֵ����,�������ֻ������ֵ�ֵ����

			return var.getValue();// String

		} else
			return null;

	}

	public static Object getRightOperand(VarBinds var, String opr,
			Object parameters, boolean needAddressTrans)
			throws IllegalAccessException {
		if (var == null)
			return null;

		if (var.getType().equals(Constants.VAR_CONSTANT)) {// ����
			if (Constants.nonSingleOprands.contains(opr))// �ǵ�ֵ����
			{
				if (needAddressTrans) {
					String[] inParams = (String[]) var.getValues();
					for (int i = 0; i < inParams.length; i++) {
						try {
							inParams[i] = InetAddress.getByName(inParams[i])
									.getHostAddress();
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
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
		} else if (var.getType().equals(Constants.VAR_EVENT)) {//

			return getEventOperand(var, parameters);

		} else if (var.getType().equals(Constants.VAR_FILTER)) {//

			return parameters;

		} else if (var.getType().equals(Constants.VAR_RESOURCE)) {//

			return var.getRef();

		} else if (var.getType().equals(Constants.VAR_INTERN)) {//

			return var.getInternValueObject();

		} else if (var.getType().equals(Constants.VAR_FILTERPLUGIN)) {//
			List arguments = new ArrayList();
			arguments.add(parameters);
			arguments.add(var.getValues());
			return arguments;

		} else if (var.getType().equals(Constants.VAR_BWLIST)) {//
			List arguments = new ArrayList();
			String[] bwlistID = new String[1];
			bwlistID[0] = var.getRef();
			arguments.add(parameters);
			arguments.add(bwlistID);
			return arguments;

		} else
			return null;

	}

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

	public static boolean cal(Object left, String opr, Object right) {
		LogicUnit lu = oprs.get(opr);
		if (lu == null)
			throw new IllegalArgumentException("undefined opr:" + opr);
		return lu.judge(left, right);
	}

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

	public static void generateSourceCodeForSIMEventObject() {
		String blank1 = "	";
		String blank2 = "		";
		String blank3 = "			";
		String rn = "\n";

		StringBuffer sourceCode = new StringBuffer();
		sourceCode
				.append(blank1
						+ "//-----------------���´���ͨ��printSourceCodeForGetAttribute()�Զ����----------------------------------------"
						+ rn);
		sourceCode.append(blank1 + "/**" + rn);
		sourceCode.append(blank1 + " * SIMEventObject������Ƶ�Сд������������ձ�"
				+ rn);
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
		sourceCode.append(blank1
				+ " * ���������ƣ���ΪСд��������¼������Ӧ������ֵ�� " + rn);
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

		sourceCode.append(blank3 + "default: return null;" + rn);
		sourceCode.append(blank2 + "}" + rn);
		sourceCode.append(blank1 + "}" + rn);
		sourceCode.append(blank1 + "" + rn);

		sourceCode.append(blank1 + "/**" + rn);
		sourceCode.append(blank1 + " * ��ݸ澯�¼�����׼����Ҫ��Ҫִ�и澯�����Ĳ���? *"
				+ rn);
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
		sourceCode.append(blank2 + "return params;" + rn);
		sourceCode.append(blank1 + "}" + rn);

		sourceCode.append(blank1 + "/**" + rn);
		sourceCode.append(blank1 + " * ����¼����񣬿������µĸ澯�����С� " + rn);
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
						+ "//-----------------�Զ���ɴ������-----------------------------------------"
						+ rn);
		System.out.println(sourceCode);
	}

	public static Map fieldNameIndexMap = new HashMap();
	static {
		fieldNameIndexMap.put("id", 0);
		fieldNameIndexMap.put("recepttime", 1);
		fieldNameIndexMap.put("aggregatedcount", 2);
		fieldNameIndexMap.put("systype", 3);
		fieldNameIndexMap.put("collectoraddr", 4);
		fieldNameIndexMap.put("collectorname", 5);
		fieldNameIndexMap.put("collecttype", 6);
		// fieldNameIndexMap.put("sensoraddr", 7);
		// fieldNameIndexMap.put("sensorname", 8);
		fieldNameIndexMap.put("name", 7);
		fieldNameIndexMap.put("category", 8);
		fieldNameIndexMap.put("type", 9);
		fieldNameIndexMap.put("priority", 10);
		// fieldNameIndexMap.put("oripriority", 13);
		fieldNameIndexMap.put("rawid", 11);
		fieldNameIndexMap.put("occurtime", 12);
		// fieldNameIndexMap.put("sendtime", 16);
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
		// fieldNameIndexMap.put("sname", 29);
		fieldNameIndexMap.put("sport", 25);
		// fieldNameIndexMap.put("sprocess", 31);
		// fieldNameIndexMap.put("suserid", 32);
		fieldNameIndexMap.put("susername", 26);
		fieldNameIndexMap.put("staddr", 27);
		fieldNameIndexMap.put("stport", 28);
		fieldNameIndexMap.put("daddr", 29);
		// fieldNameIndexMap.put("dname", 37);
		fieldNameIndexMap.put("dport", 30);
		// fieldNameIndexMap.put("dprocess", 39);
		// fieldNameIndexMap.put("duserid", 40);
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
		// fieldNameIndexMap.put("program", 51);
		fieldNameIndexMap.put("sessionid", 41);
		fieldNameIndexMap.put("requesturi", 42);
		fieldNameIndexMap.put("msg", 53);
		fieldNameIndexMap.put("customs1", 43);
		fieldNameIndexMap.put("customs2", 44);
		fieldNameIndexMap.put("customs3", 45);
		fieldNameIndexMap.put("customs4", 46);
		// fieldNameIndexMap.put("customs5", 59);
		// fieldNameIndexMap.put("customs6", 60);
		// fieldNameIndexMap.put("customs7", 61);
		fieldNameIndexMap.put("smac", 47);
		fieldNameIndexMap.put("dmac", 48);
		fieldNameIndexMap.put("customd1", 49);
		fieldNameIndexMap.put("customd2", 50);
		fieldNameIndexMap.put("customd3", 51);
		fieldNameIndexMap.put("customd4", 52);
	}

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
			// case 7: return event.sensorAddr;
			// case 8: return event.sensorName;
		case 7:
			return event.name;
		case 8:
			return event.category;
		case 9:
			return event.type;
		case 10:
			return event.priority;
			// case 13: return event.oriPriority;
		case 11:
			return event.rawID;
		case 12:
			return event.occurTime;
			// case 16: return event.sendTime;
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
				// TODO Auto-generated catch block
				return event.sAddr;
			}
			// case 29: return event.sName;
		case 25:
			return event.sPort;
			// case 31: return event.sProcess;
			// case 32: return event.sUserID;
		case 26:
			return event.sUserName;
		case 27:
			try {
				return InetAddress.getByName(event.stAddr).getHostAddress();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				return event.stAddr;
			}
		case 28:
			return event.stPort;
		case 29:
			try {
				return InetAddress.getByName(event.dAddr).getHostAddress();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				return event.dAddr;
			}
			// case 37: return event.dName;
		case 30:
			return event.dPort;
			// case 39: return event.dProcess;
			// case 40: return event.dUserID;
		case 31:
			return event.dUserName;
		case 32:
			try {
				return InetAddress.getByName(event.dtAddr).getHostAddress();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
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
			// case 51: return event.program;
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
			// case 59: return event.customS5;
			// case 60: return event.customS6;
		case 47:
			return event.sMAC;
			// case 61: return event.customS7;
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

	public static void main(String[] args) {
		generateSourceCodeForSIMEventObject();
	}
}

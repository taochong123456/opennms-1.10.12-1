package org.opennms.netmgt.syslogd.analyze;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.jdom.CDATA;
import org.jdom.Element;
import org.opennms.netmgt.syslogd.parse.SIMEventObject;

class QNode {
	Element element;

	ExpressionNode eNode;

	public QNode() {
	}

	public QNode(Element element, ExpressionNode eNode) {
		this.element = element;
		this.eNode = eNode;
	}
}

/**
 * 类型转换,该类提供静态方法,将xml描述的规则或过滤器与相应的java对象进行转换.
 * 
 * @author zhuzhen
 * 
 */
public class TransformHelper {

	/**
	 * 日志接口。
	 */
	private static Logger logger = Logger.getLogger(TransformHelper.class);

	/**
	 * 根据rule对象，构造对应的xml元素。
	 * 
	 * @param rule
	 * @return 如果rule中有不合规方的地方，则会返回null
	 */
	private static Element makeRuleXMLElement(SIMRuleObject rule) {
		// -- 验证过滤器id --
		String id = rule.getRuleID();
		if (!StringTool.hasContent(id)) {
			logger.info("Translate from ruleObject To xml error! Because rule id is blank.:\n");
			return null;
		}

		// -- 设置xml中的filter元素和属性 --
		Element ruleElement = new Element("rule");
		ruleElement.setAttribute("id", id);
		if (StringTool.hasContent((rule.getName()))) {
			ruleElement.setAttribute("name", rule.getName());
		}

		// -- 处理define区 --
		Map<String, SIMFilterObject> filterMaps = rule.getFilterMaps();
		if (filterMaps != null && filterMaps.size() != 0) {
			List<Element> filterElements = new ArrayList<Element>();
			Element defineElement = new Element("define");
			ruleElement.setContent(defineElement);
			Set keySet = filterMaps.keySet();
			for (Iterator iter = keySet.iterator(); iter.hasNext();) {
				String filterID = (String) iter.next();
				SIMFilterObject filter = filterMaps.get(filterID);
				if (filter != null) {
					filterElements.add(makeFilterXMLElement(filter));
				}
			}
			defineElement.addContent(filterElements);
		}

		// -- 处理process区 --
		Element processElement = new Element("process");
		ruleElement.addContent(processElement);

		// -- 处理timescope区 --
		Element timeScopeElement = new Element("timescope");
		processElement.addContent(timeScopeElement);
		TimeScope ts = rule.getTimeScope();
		if (ts != null) {
			timeScopeElement.setAttribute("length",
					String.valueOf(ts.getLength()));
			timeScopeElement.setAttribute("unit", String.valueOf(ts.getUnit()));
			timeScopeElement.setAttribute("frequency",
					String.valueOf(ts.getFrequency()));
		}
		// -- 处理pattern 区 --
		VarBinds patternVar = rule.getPatternVar();
		if (patternVar != null) {
			Element patternElement = new Element("pattern");
			timeScopeElement.addContent(patternElement);
			Element varElement = makeVarBindsXMLElement(patternVar);
			patternElement.addContent(varElement);
		}

		// -- 处理point 区 --
		List<Element> varElements = makeVarBindsXMLElementList(rule
				.getPointVars());
		if (varElements != null && varElements.size() > 0) {
			Element pointElement = new Element("point");
			timeScopeElement.addContent(pointElement);
			pointElement.addContent(varElements);
		}

		// -- 处理identical 区 --
		varElements = makeVarBindsXMLElementList(rule.getIdVars());
		if (varElements != null && varElements.size() > 0) {
			Element identicalElement = new Element("identical");
			timeScopeElement.addContent(identicalElement);
			identicalElement.addContent(varElements);
		}

		// -- 处理different 区 --
		varElements = makeVarBindsXMLElementList(rule.getDiffVars());
		if (varElements != null && varElements.size() > 0) {
			Element differentElement = new Element("different");
			timeScopeElement.addContent(differentElement);
			differentElement.addContent(varElements);
		}

		// -- 处理actions区 --
		List<MActionObject> actions = rule.getActions();
		if (actions != null && actions.size() > 0) {
			Element actionsElement = new Element("actions");
			processElement.addContent(actionsElement);
			for (int i = 0; i < actions.size(); i++) {
				MActionObject action = actions.get(i);
				if (action != null)
					actionsElement.addContent(makeActionXMLElement(action));
			}
		}

		// -- 处理fireinterval区 --
		TimeScope fireIntervalScope = rule.getFireInterval();
		if (fireIntervalScope != null && fireIntervalScope.getLength() != 0) {
			Element fireIntervalScopeElement = new Element("fireinterval");
			processElement.addContent(fireIntervalScopeElement);
			fireIntervalScopeElement.setAttribute("length",
					String.valueOf(fireIntervalScope.getLength()));
			fireIntervalScopeElement.setAttribute("unit",
					String.valueOf(fireIntervalScope.getUnit()));
		}
		return ruleElement;
	}

	/**
	 * 构造varbinds的xml元素列表
	 * 
	 * @param vars
	 * @return
	 */
	private static List<Element> makeVarBindsXMLElementList(List<VarBinds> vars) {
		if (vars == null || vars.size() == 0)
			return null;
		List<Element> varElements = new ArrayList<Element>();
		for (int i = 0; i < vars.size(); i++) {
			VarBinds var = vars.get(i);
			Element varElement = makeVarBindsXMLElement(var);
			if (varElement != null)
				varElements.add(varElement);
		}
		return varElements;
	}

	private static Element makeActionXMLElement(MActionObject action) {
		Element actionElement = new Element("action");

		String type = action.getType();
		String position = action.getPosition();
		if (StringTool.hasContent(type)) {
			actionElement
					.setAttribute("type", type = StringTool.toShrink(type));
		}
		if (StringTool.hasContent(position)) {
			actionElement.setAttribute("position",
					position = StringTool.toShrink(position));
		}

		String[][] params = action.getParams();
		if (params != null) {
			Element parameters = new Element("parameters");
			for (int k = 0; k < params.length; k++) {
				String name = "";
				String value = "";
				try {
					name = params[k][0];
					value = params[k][1];
				} catch (Exception e) {
				}
				if (name == null)
					name = "";// 不能确保是否被正确赋值，所以需要判断
				if (value == null)
					value = "";// 不能确保是否被正确赋值，所以需要判断

				Element nameElement = new Element("name");
				nameElement.setText(name.trim());
				Element valueElement = new Element("value");
				valueElement.setText(value.trim());
				Element parameter = new Element("parameter");
				parameter.addContent(nameElement);
				parameter.addContent(valueElement);
				parameters.addContent(parameter);
			}
			actionElement.addContent(parameters);
		}
		return actionElement;

	}

	/**
	 * 将规则对像转换为xml的描述
	 * 
	 * @param rule
	 * @return
	 */
	public static String transFromRuleObjectToXML(SIMRuleObject rule) {
		// -- 参数校验 --
		if (rule == null)
			return null;

		// -- 生成xml字符串 --
		XmlManager xmlOperator = new XmlManager();
		try {
			// -- 此句话的目的是初始化xmlOperator的document属性。
			if (!xmlOperator.read(new StringReader("<a></a>")))
				return null;

			Element ruleElement = makeRuleXMLElement(rule);
			xmlOperator.setRootElement(ruleElement);

			StringWriter writer = new StringWriter();
			xmlOperator.write(writer);

			return writer.toString();

		} catch (Exception ex) {
			logger.error("translate from rule obejct to xml error!", ex);
			return null;
		}
	}

	/**
	 * 根据xml字符串，构造SIMRuleObject对象
	 * 
	 * @param ruleElement
	 * @param xmlStr
	 * @return
	 */
	private static SIMRuleObject makeRuleObject(Element ruleElement,
			String xmlStr) {
		if (ruleElement == null) {
			logger.info("Can not transform filter xml string to rule object!Because xml string is blank.");
			return null;
		}

		String id = ruleElement.getAttributeValue("id");// 获取"<rule
		// id='xxx'..." 的id
		if (!StringTool.hasContent(id)) {// id必须有值
			logger.info("Translate from xml To ruleObject error! Because filter id is blank.:\n"
					+ xmlStr + "\n");
			return null;
		}

		// -- 构造ruleobject --
		SIMRuleObject simRuleOjbect = new SIMRuleObject();
		simRuleOjbect.setRuleID(id);
		simRuleOjbect.setName(ruleElement.getAttributeValue("name"));// 获取"<filter
		// name='xxx'..."
		// 的name值，可以为空

		// -- 处理define区 --
		Element defineElement = ruleElement.getChild("define"); // 处理define区
		if (defineElement != null) {
			List filterElements = defineElement.getChildren();
			if (filterElements != null) {
				for (int i = 0; i < filterElements.size(); i++) {
					Element filterElement = (Element) filterElements.get(i);
					SIMFilterObject filter = makeFilterObject(filterElement,
							xmlStr);// 构造define区中的filter对象
					if (filter != null) {
						simRuleOjbect.getFilterMaps().put(filter.getId(),
								filter);
					} else {
						logger.error("Error when making filter object at define area from rule xml "
								+ xmlStr + filterElement);
					}
				}
			}
		}

		// -- 处理 process 区 --
		Element processElement = ruleElement.getChild("process");
		if (processElement != null) {
			// -- 处理 timescope区 --
			Element timeScopeElement = processElement.getChild("timescope");
			if (timeScopeElement != null) {
				// -- 处理时间频率属性 --
				TimeScope ts = new TimeScope();

				try {// length
					int length = Integer.parseInt(timeScopeElement
							.getAttributeValue("length"));
					ts.setLength(length);
				} catch (Exception e) {
					logger.error("Error format when translating timescope length! "
							+ xmlStr);
				}

				String unit = timeScopeElement.getAttributeValue("unit");// unit

				try {// frequency
					int frequency = Integer.parseInt(timeScopeElement
							.getAttributeValue("frequency"));
					ts.setFrequency(frequency);
				} catch (Exception e) {
					logger.error("Error format when translating timescope frequency! "
							+ xmlStr);
				}

				simRuleOjbect.setTimeScope(ts);

				// -- 处理pattern区 --
				Element patternElement = timeScopeElement.getChild("pattern");
				if (patternElement != null) {
					Element varElement = patternElement.getChild("var");
					if (varElement != null) {
						simRuleOjbect
								.setPatternVar(makeVarBindsObject(varElement));
					}
				}
				// -- 处理point区--
				Element pointElement = timeScopeElement.getChild("point");
				List<VarBinds> pointVars = makeVarBindsList(pointElement);
				simRuleOjbect.setPointVars(pointVars);

				// -- 处理identical区--
				Element identicalElement = timeScopeElement
						.getChild("identical");
				List<VarBinds> idVars = makeVarBindsList(identicalElement);
				simRuleOjbect.setIdVars(idVars);

				// -- 处理different区--
				Element differentElement = timeScopeElement
						.getChild("different");
				List<VarBinds> diffVars = makeVarBindsList(differentElement);
				simRuleOjbect.setDiffVars(diffVars);

			}
			// -- 处理 action区 --
			Element actionsElement = processElement.getChild("actions");
			if (actionsElement != null) {
				simRuleOjbect.setActions(makeActionList(actionsElement));
			}

			// -- 处理 fireinterval区 --
			Element fireIntervalElement = processElement
					.getChild("fireinterval");
			if (fireIntervalElement != null) {
				TimeScope fireIntervalScope = new TimeScope();
				try {// length
					int length = Integer.parseInt(fireIntervalElement
							.getAttributeValue("length"));
					fireIntervalScope.setLength(length);
				} catch (Exception e) {// 如果该值有误，则置为0，相当于没有预警间隔
					fireIntervalScope.setLength(0);
				}
				String unit = fireIntervalElement.getAttributeValue("unit");// unit

				fireIntervalScope.setUnit(unit);
				simRuleOjbect.setFireInterval(fireIntervalScope);
			}

		}

		return simRuleOjbect;
	}

	/**
	 * 构造action列表
	 * 
	 * @param actions
	 * @return
	 */
	private static List<MActionObject> makeActionList(Element actions) {

		List<Element> actionList = actions.getChildren("action");
		if (actionList == null || actionList.size() == 0)
			return null;

		List<MActionObject> actionsInAlertRuleObject = new ArrayList();
		for (int i = 0; i < actionList.size(); i++) {

			Element action = actionList.get(i);
			if (action == null)
				continue;
			MActionObject actionObject = new MActionObject();
			String type = action.getAttributeValue("type");
			// -- 必须有type属性
			if (StringTool.hasContent(type)) {
				actionObject.setType(type = StringTool.toShrink(type));

				String position = action.getAttributeValue("position");
				if (StringTool.hasContent(position))
					actionObject.setPosition(position = StringTool
							.toShrink(position));

				Element parameters = action.getChild("parameters");
				if (parameters != null) {
					List<Element> parametersList = parameters
							.getChildren("parameter");
					if (parametersList != null) {
						String[][] params = new String[parametersList.size()][2];
						for (int j = 0; j < parametersList.size(); j++) {
							Element parameter = parametersList.get(j);
							String name = parameter.getChildText("name");
							if (StringTool.hasContent(name))
								params[j][0] = name.trim();
							else
								params[j][0] = "";
							String value = parameter.getChildText("value");
							if (StringTool.hasContent(value))
								params[j][1] = value.trim();
							else
								params[j][1] = "";
						}
						actionObject.setParams(params);
					}
				}
				actionsInAlertRuleObject.add(actionObject);
			}
		}
		return actionsInAlertRuleObject;
	}

	/**
	 * 获取var列表
	 * 
	 * @param element
	 * @return
	 */
	private static List<VarBinds> makeVarBindsList(Element element) {
		if (element != null) {
			List varElements = element.getChildren("var");
			if (varElements != null && varElements.size() != 0) {
				List<VarBinds> pointVars = new ArrayList<VarBinds>();
				for (int i = 0; i < varElements.size(); i++) {
					VarBinds var = makeVarBindsObject((Element) varElements
							.get(i));
					if (var != null) {
						pointVars.add(var);
					}
				}
				return pointVars;
			}
		}
		return null;
	}

	/**
	 * 将xml的描述转换为规则对像
	 * 
	 * @param ruleXMLStr
	 * @return
	 */
	public static SIMRuleObject transFromXMLToRuleObject(String ruleXMLStr) {
		// -- 分析xml字符串 --
		XmlManager xmlOperator = new XmlManager();
		try {
			if (!xmlOperator.read(new StringReader(ruleXMLStr)))
				return null;
			Element ruleElement = xmlOperator.getRootElement();
			return makeRuleObject(ruleElement, ruleXMLStr);

		} catch (Exception ex) {
			logger.error("Translate from xml To ruleObject error! Because "
					+ ex.getMessage() + "\n" + ruleXMLStr + "\n");
		}
		return null;
	}

	/**
	 * 根据filter对象，构造对应的xml元素。
	 * 
	 * @param filter
	 * @return 如果filter中有不合规方的地方，则会返回null
	 */
	private static Element makeFilterXMLElement(SIMFilterObject filter) {

		// -- 验证过滤器id --
		String id = filter.getId();
		if (!StringTool.hasContent(id)) {
			logger.info("Translate from filterObject To xml error! Because filter id is blank.:\n");
			return null;
		}

		// -- 设置xml中的filter元素和属性 --
		Element filterElement = new Element("filter");
		filterElement.setAttribute("id", id);
		if (StringTool.hasContent((filter.getName()))) {
			filterElement.setAttribute("name", filter.getName());
		}

		// -- 构造逻辑表达式的xml元素
		ExpressionNode root = filter.getRoot();
		if (root != null) {

			Element conditionElement = new Element("condition");
			filterElement.setContent(conditionElement);

			Queue<QNode> queue = new LinkedList<QNode>();// 先进先出队列，采用树的宽度遍历算法，构造逻辑表达式。
			queue.add(new QNode(conditionElement, root));

			do {
				QNode qNode = queue.poll();// 从队列头获取结点
				if (qNode == null)
					break;

				ExpressionNode eNode = qNode.eNode;
				Element element = qNode.element;

				String type = eNode.getType();
				if (StringTool.hasContent(type)) {
					if (type.equalsIgnoreCase(Constants.EXP_BASE)) {// 构造基本表达式

						makeBaseExpressionXMLElement(qNode);

					} else {// 构造“与或非“复合表达式

						List<ExpressionNode> children = eNode.getChildren();

						if (children != null && children.size() != 0) {
							element.setAttribute("type", type);

							List<Element> expressionElements = new ArrayList<Element>();
							for (int i = 0; i < children.size(); i++) {
								ExpressionNode childENode = children.get(i);
								Element childElement = new Element("condition");
								expressionElements.add(childElement);// 建立表达式树的关键语句
								queue.add(new QNode(childElement, childENode));
							}
							element.setContent(expressionElements);

						} else {
							logger.info("Translate from filterObject To xml error! Because short of child expression. filter id="
									+ filter.getId());
							return null;
						}
					}

				} else {
					logger.info("Translate from filterObject To xml error! Because expression type is null. ");
					return null;
				}

			} while (true);

		}
		return filterElement;
	}

	/**
	 * 构造最基本的xml element表达式单元。
	 * 
	 * @param qNode
	 */
	private static void makeBaseExpressionXMLElement(QNode qNode) {

		ExpressionNode eNode = qNode.eNode;
		Element element = qNode.element;

		element.setAttribute("type", Constants.EXP_BASE);// 设置表达式类型，"base"
		String opr = eNode.getOpr();

		if (StringTool.hasContent(opr)) {
			element.setAttribute("opr", opr.trim().toLowerCase());// 设置逻辑运算符

			VarBinds left = eNode.getLeft();
			if (left == null)// 左侧表达式必须存在
				throw new NullPointerException(
						"short of left varbinds in base expression node.");
			Element leftVarElement = makeVarBindsXMLElement(left);// 构造表达式左侧xml元素
			if (leftVarElement != null)
				element.addContent(leftVarElement);
			else
				throw new NullPointerException(
						"Error when making left varbinds element from  base expression node.");

			VarBinds right = eNode.getRight();
			if (right != null) {// 右侧表达式可以不存在
				Element rightVarElement = makeVarBindsXMLElement(right);// 构造表达式右侧xml元素
				if (rightVarElement != null)
					element.addContent(rightVarElement);
			}

		} else {
			throw new NullPointerException(
					"short of opr in base expression node.");
		}
	}

	/**
	 * 构造xml表达式的操作数元素
	 * 
	 * @param varBind
	 * @return
	 */
	private static Element makeVarBindsXMLElement(VarBinds varBind) {
		String type = varBind.getType();// 获得操作数类型，比如event,filter,constant...
		if (StringTool.hasContent(type)) {
			type = type.trim().toLowerCase();
			Element varBindElement = new Element("var");
			varBindElement.setAttribute("type", type);// 设置操作数类型
			// if (type.equals("constant")) {//只要有值，就加入到xml中
			String[] values = varBind.getValues();
			if (values != null && values.length > 0) {
				for (int i = 0; i < values.length; i++) {
					Element valueElement = new Element("value");

					CDATA cData = new CDATA(values[i]);
					valueElement.addContent(cData);
					// valueElement.setText(values[i]);

					varBindElement.addContent(valueElement);
				}
			}

			// } else {
			String ref = varBind.getRef();
			String attrName = varBind.getAttrname();
			if (StringTool.hasContent(ref))
				varBindElement.setAttribute("ref", ref);
			if (StringTool.hasContent(attrName))
				varBindElement.setAttribute("attrname", attrName.toLowerCase());
			// }
			return varBindElement;

		} else {
			throw new NullPointerException(
					"short of type in varbinds from varbind object.");
		}

	}

	/**
	 * 将过滤器对像转换为xml的描述
	 * 
	 * @param filter
	 * @return
	 */
	public static String transFromFilterObjectToXML(SIMFilterObject filter) {
		// -- 参数校验 --
		if (filter == null)
			return null;

		// -- 生成xml字符串 --
		XmlManager xmlOperator = new XmlManager();
		try {
			// -- 此句话的目的是初始化xmlOperator的document属性。
			if (!xmlOperator.read(new StringReader("<a></a>")))
				return null;

			Element filterElement = makeFilterXMLElement(filter);
			xmlOperator.setRootElement(filterElement);

			StringWriter writer = new StringWriter();
			xmlOperator.write(writer);

			return writer.toString();

		} catch (Exception ex) {
			logger.error("translate from filter obejct to xml error!", ex);
			return null;
		}

	}

	/**
	 * 根据xml的filter元素构造过滤器对象。
	 * 
	 * @param filterElement
	 * @param xmlStr
	 *            原始文本
	 * @return
	 */
	private static SIMFilterObject makeFilterObject(Element filterElement,
			String xmlStr) {
		if (filterElement == null) {
			logger.info("Can not transform filter xml string to filter object!Because xml string is blank.");
			return null;
		}

		String id = filterElement.getAttributeValue("id");// 获取"<filter
		// id='xxx'..." 的id
		if (!StringTool.hasContent(id)) {// id必须有值
			logger.info("Translate from xml To filterObject error! Because filter id is blank.:\n"
					+ xmlStr + "\n");
			return null;
		}

		// -- 构造filterobject --
		SIMFilterObject simFilterOjbect = new SIMFilterObject();
		simFilterOjbect.setId(id);
		simFilterOjbect.setName(filterElement.getAttributeValue("name"));// 获取"<filter
		// name='xxx'..."
		// 的name值，可以为空

		Element expressionElement = filterElement.getChild("condition");// 表达式的XML根元素
		if (expressionElement != null) {// 该判断说明SIMFilterObject中的根表达式，可能为空。

			ExpressionNode expressionNode = new ExpressionNode();
			simFilterOjbect.setRoot(expressionNode); // 设置表达式的根

			Queue<QNode> queue = new LinkedList<QNode>();// 先进先出队列，采用树的宽度遍历算法，构造逻辑表达式。
			queue.add(new QNode(expressionElement, expressionNode));

			do {
				QNode qNode = queue.poll();// 从队列头获取结点
				if (qNode == null)
					break;

				Element element = qNode.element;
				ExpressionNode eNode = qNode.eNode;

				String type = element.getAttributeValue("type");// 获取表达式类型
				if (StringTool.hasContent(type)) {

					if (type.equalsIgnoreCase(Constants.EXP_BASE)) {// 构造基本表达式
						makeBaseExpressionNodeObject(qNode);
					} else {// 构造“与或非“复合表达式

						List children = element.getChildren("condition");
						if (children != null && children.size() != 0) {

							eNode.setType(type.trim().toLowerCase());
							List<ExpressionNode> expressionNodes = new ArrayList<ExpressionNode>();
							for (int i = 0; i < children.size(); i++) {
								Element childElement = (Element) children
										.get(i);
								ExpressionNode childENode = new ExpressionNode();
								expressionNodes.add(childENode);// 建立表达式树的关键语句
								queue.add(new QNode(childElement, childENode));
							}
							eNode.setChildren(expressionNodes);

						} else {
							logger.info("Translate from xml To filterObject error! Because short of child expression. \n"
									+ xmlStr + "\n");
							return null;
						}
					}

				} else {
					logger.info("Translate from xml To filterObject error! Because expression type is null. \n"
							+ xmlStr + "\n");
					return null;
				}

			} while (true);
		}
		return simFilterOjbect;
	}

	/**
	 * 构造最基本的expression node表达式单元。
	 * 
	 * @param qNode
	 */
	private static void makeBaseExpressionNodeObject(QNode qNode) {

		Element element = qNode.element;
		ExpressionNode eNode = qNode.eNode;
		eNode.setType(Constants.EXP_BASE);// 设置表达式类型，"base"

		String opr = element.getAttributeValue("opr");
		if (StringTool.hasContent(opr)) {
			eNode.setOpr(opr.trim().toLowerCase());// 设置逻辑运算符，比如eq,in,matches...
			List varBindsList = element.getChildren("var");// 获取表达式左右的两个操作数
			if (varBindsList != null && varBindsList.size() != 0) {
				VarBinds left = makeVarBindsObject((Element) varBindsList
						.get(0));// 构造左操作数
				VarBinds right = null;
				if (varBindsList.size() >= 2)
					right = makeVarBindsObject((Element) varBindsList.get(1));// 构造右操作数
				eNode.setLeft(left);
				eNode.setRight(right);

			} else {
				throw new NullPointerException(
						"short of varbinds in base expression element.");
			}

		} else {
			throw new NullPointerException(
					"short of opr in base expression element.");
		}

	}

	/**
	 * 构造操作数
	 * 
	 * @param element
	 * @return
	 */
	private static VarBinds makeVarBindsObject(Element element) {
		String type = element.getAttributeValue("type");// 获得操作数类型，比如event,filter,constant...
		if (StringTool.hasContent(type)) {
			VarBinds var = new VarBinds();
			var.setType(type.trim().toLowerCase());// 设置操作数类型
			// if (var.getType().equalsIgnoreCase("constant")) {//有什么参数就设置什么参数
			List valueList = element.getChildren("value");
			if (valueList != null && valueList.size() != 0)
			// {
			{
				String[] values = new String[valueList.size()];
				for (int i = 0; i < valueList.size(); i++) {
					values[i] = ((Element) valueList.get(i)).getValue();// 添加常量值
					// values[i] = ((Element) valueList.get(i)).getText();//
					// 添加常量值

					if (values[i] != null)
						values[i] = values[i].trim();
				}
				var.setValues(values);
			}
			// } else {
			// throw new NullPointerException(
			// "short of constant value in varbinds.");
			// }
			// } else {// 非常量操作数
			String ref = element.getAttributeValue("ref");
			if (StringTool.hasContent(ref)) {
				ref = ref.trim();
				var.setRef(element.getAttributeValue("ref"));
			}
			String attrNameStr = element.getAttributeValue("attrname");
			if (StringTool.hasContent(attrNameStr)) {
				attrNameStr = attrNameStr.trim().toLowerCase();
				var.setAttrname(attrNameStr);
			}
			// }
			return var;
		} else {
			throw new NullPointerException(
					"short of type in varbinds from xml element.");
		}

	}

	/**
	 * 将xml的描述转换为过滤器对像. 注意：转换后的fitler对象，一定具有id属性。name和root属性可能为空。root是一棵表达式树。
	 * 
	 * @param filterXMLStr
	 * @return 返回根据参数字符串构造的过滤器对象，如果分析过程中有任何问题，则会返回null.
	 */
	public static SIMFilterObject transFromXMLToFilterObject(String filterXMLStr) {
		// -- 分析xml字符串 --
		XmlManager xmlOperator = new XmlManager();
		try {
			if (!xmlOperator.read(new StringReader(filterXMLStr)))
				return null;
			Element filterElement = xmlOperator.getRootElement();
			return makeFilterObject(filterElement, filterXMLStr);

		} catch (Exception ex) {
			logger.error("Translate from xml To filterObject error! Because "
					+ ex.getMessage() + "\n" + filterXMLStr + "\n");
		}
		return null;
	}

	/**
	 * 将端口字符串表示为PortResource对象
	 * 
	 * @param portString
	 * @return
	 */
	public static PortResource transFromStringToPortResource(String portString) {
		Parser p = new Parser(false);
		p.parse(new StringReader(portString + "\n"));
		return p.getPortResource();
	}

	/**
	 * 将端口资源对象转换为字符串
	 * 
	 * @param pr
	 * @return
	 */
	public static String transFromPortResourceToString(PortResource pr) {
		StringBuffer sb = new StringBuffer();
		ResourceTransfer.printPortResource(pr, sb);
		return sb.toString();
	}

	/**
	 * 将IP范围字符串表示为IPResource对象
	 * 
	 * @param ipString
	 * @return
	 */
	public static IPResource transFromStringToIPResource(String ipString) {
		IPParser p = new IPParser(false);
		p.parse(new StringReader(ipString + "\n"));
		return p.getIPResource();
	}

	/**
	 * 将IP资源对象转换为字符串
	 * 
	 * @param pr
	 * @return
	 */
	public static String transFromIPResourceToString(IPResource pr) {
		StringBuffer sb = new StringBuffer();
		ResourceTransfer.printIPResource(pr, sb);
		return sb.toString();
	}

	/**
	 * 将Time范围字符串表示为TimeResource对象
	 * 
	 * @param timeString
	 * @return
	 */
	public static TimeResource transFromStringToTimeResource(String timeString) {
		TimeParser p = new TimeParser(false);
		p.parse(new StringReader(timeString + "\n"));
		return p.getTimeResource();
	}

	/**
	 * 将Time资源对象转换为字符串
	 * 
	 * @param pr
	 * @return
	 */
	public static String transFromTimeResourceToString(TimeResource pr) {
		StringBuffer sb = new StringBuffer();
		ResourceTransfer.printTimeResource(pr, sb);
		return sb.toString();
	}

	/**
	 * 根据字符串创建表达式节点，目前只支持基本的运算符，不包括incategory和matchesfilter和资源相关的过滤器
	 * 字符串有如下基本形式"事件属性 操作符 操作书" 比如 devaddr eq_str 10.50.10.50 或devaddr in
	 * (1.1.1.1, 2.2.2.2)
	 * 
	 * @param condStr
	 * @return
	 */
	public static ExpressionNode constructExpressionNode(String condStr) {
		if (condStr == null)
			return null;
		String myCondStr = condStr.trim();
		if (myCondStr.length() == 0)
			return null;

		int firstOperandPosition = myCondStr.indexOf('(');

		ExpressionNode expressionNode = new ExpressionNode();
		expressionNode.setType(Constants.EXP_BASE);

		if (firstOperandPosition != -1) {// 逻辑运算符的右操作多数值格式
			String attrAndOprStr = myCondStr.substring(0, firstOperandPosition);
			String[] attrAndOprStrs = attrAndOprStr.split("\\s");// 以空白字符为分割
			if (attrAndOprStrs.length != 2)
				return null;

			// System.out.println(attrAndOprStrs[0]);
			// System.out.println(attrAndOprStrs[1]);

			// 设置左操作数
			VarBinds left = new VarBinds();
			left.setType(Constants.VAR_EVENT);
			left.setAttrname(attrAndOprStrs[0]);

			// 获取右操作数的多数值字符串表示
			String operand = myCondStr.substring(firstOperandPosition + 1,
					myCondStr.length() - 1);// 去掉首尾的括号
			String[] operands = operand.split(",");
			if (operands.length < 1)
				return null;

			// 设置右操作数
			VarBinds right = new VarBinds();
			right.setType(Constants.VAR_CONSTANT);

			List valueList = new ArrayList();// 使用valueList的目的是滤掉空值
			for (int i = 0; i < operands.length; i++) {
				String opr = operands[i];
				if (opr != null && opr.trim().length() != 0) {
					valueList.add(opr.trim());
				}
			}
			String[] values = new String[valueList.size()];
			if (values.length != 0) {
				for (int i = 0; i < valueList.size(); i++) {
					values[i] = (String) valueList.get(i);
				}
			}
			right.setValues(values);

			// 设置表达式
			expressionNode.setOpr(attrAndOprStrs[1]);
			expressionNode.setLeft(left);
			expressionNode.setRight(right);

		} else {// //逻辑运算符的右操作单值数格式

			String[] strs = myCondStr.split("\\s+", 3);// 只分三段
			if (strs.length < 3)
				return null;

			// System.out.println(strs[0]);
			// System.out.println(strs[1]);
			// System.out.println(strs[2]);

			// 设置左操作数
			VarBinds left = new VarBinds();
			left.setType(Constants.VAR_EVENT);
			left.setAttrname(strs[0]);

			// 设置右操作数
			VarBinds right = new VarBinds();
			right.setType(Constants.VAR_CONSTANT);
			right.setValue(strs[2]);

			// 设置表达式
			expressionNode.setOpr(strs[1]);
			expressionNode.setLeft(left);
			expressionNode.setRight(right);

		}

		return expressionNode;
	}

	/**
	 * 将表达式节点转换为字符串
	 * 
	 * @param expression
	 * @return
	 */
	public static String transExpressionNodeToString(ExpressionNode expression) {
		StringBuffer sb = new StringBuffer();
		if (expression != null)
			makeExpressionStr(expression, sb);
		return sb.toString();
	}

	private static final Set<String> addrFields = new HashSet<String>();
	static {
		addrFields.add("sensoraddr");
		addrFields.add("collectoraddr");
		addrFields.add("saddr");
		addrFields.add("staddr");
		addrFields.add("daddr");
		addrFields.add("dtaddr");
		addrFields.add("devaddr");
	}

	private static final boolean isIPAddr(ExpressionNode expression) {
		if (expression != null && expression.getLeft() != null
				&& expression.getLeft().getAttrname() != null)
			return addrFields.contains(expression.getLeft().getAttrname()
					.toLowerCase());
		return false;
	}

	/**
	 * Add by liyue 2012-11-19 增加查询条件是否为设备类型字段的验证
	 * 
	 * @param expression
	 * @return
	 */
	private static final boolean isDevType(ExpressionNode expression) {
		if (expression != null && expression.getLeft() != null
				&& expression.getLeft().getAttrname() != null)
			return "devtype".equalsIgnoreCase(expression.getLeft()
					.getAttrname().toLowerCase());
		return false;
	}

	/**
	 * 根据要查询的设备类型，确定具体的查询范围 如，查询安全设备，即400-499的范围 列表如下：
	 * -------------------------- 未知设备：0 网络设备：100-199 工作站：300-399 安全设备：400-499
	 * 存储设备：500-599 机房设备：700-799 其他设备：600-699 中间件：800-899 数据库：900-999
	 * 防病毒系统：1000-1099 服务器：1200-1299 其他业务系统：1300-1399 --------------------------
	 * 
	 * @return
	 */
	private static final String getDevTypeCondition(String rightStr) {
		int rightInt = Integer.parseInt(rightStr);
		switch (rightInt) {
		case 0:
			return " 0 and 0 ";
		case 100:
			return " 100 and 199 ";
		case 200:
			return " 200 and 299 ";
		case 300:
			return " 300 and 399 ";
		case 400:
			return " 400 and 499 ";
		case 500:
			return " 500 and 599 ";
		case 600:
			return " 600 and 699 ";
		case 700:
			return " 700 and 799 ";
		case 800:
			return " 800 and 899 ";
		case 900:
			return " 900 and 999 ";
		case 1000:
			return " 1000 and 1099 ";
		case 1100:
			return " 1100 and 1199 ";
		case 1200:
			return " 1200 and 1299 ";
		case 1300:
			return " 1300 and 1399 ";
		}
		return "";
	}

	private static final long[] analyseIP(String ip) {
		return new long[] { 0 };
	}

	/**
	 * 表达式左侧是否为时间字段,且操作符为 =/<>/属于(innumber)/<=/>/<
	 * 需要特殊处理,因为时间选择时，精确到秒，但数据存贮时是精确到毫秒
	 * 
	 * @param expression
	 *            表达式
	 * @return
	 */
	private static boolean isSpecialTime(ExpressionNode expression) {
		VarBinds leftNode = null;
		String leftNodeName = null;
		String operator = null;
		if (expression != null && (leftNode = expression.getLeft()) != null) {
			operator = expression.getOpr();
			if ((leftNodeName = leftNode.getAttrname()) != null
					&& operator != null) {
				if (TIME_FIELD_SET.contains(leftNodeName)
						&& (operator.equals(Constants.OPR_EQ)
								|| operator.equals(Constants.OPR_NE)
								|| operator.equals(Constants.OPR_INNUMBER)
								|| operator.equals(Constants.OPR_LE)
								|| operator.equals(Constants.OPR_LT) || operator
									.equals(Constants.OPR_GT)))
					return true;
			}
		}

		return false;
	}

	/**
	 * 时间类型的属性名字集合
	 */
	private static final Set<String> TIME_FIELD_SET = new HashSet<String>();
	static {
		TIME_FIELD_SET.add("recepttime");
		TIME_FIELD_SET.add("sendtime");
		TIME_FIELD_SET.add("occurtime");
		TIME_FIELD_SET.add("duration");
	}

	public static String transExpressionNodeToSQLString(
			ExpressionNode expression) {
		return transExpressionNodeToSQLString(expression, null);
	}

	public static String transExpressionNodeToSQLString(
			ExpressionNode expression, String tableAlias) {
		StringBuffer sb = new StringBuffer();
		if (expression != null)
			makeExpressionSQLStr(expression, sb, tableAlias);
		return sb.toString();
	}

	/**
	 * 将表达式节点转换为字符串,该方法内部使用。
	 * 
	 * @param expression
	 * @return
	 */
	private static void makeExpressionStr(ExpressionNode expression,
			StringBuffer sb) {
		if (expression.getType().equalsIgnoreCase(Constants.EXP_BASE)) {
			makeSingleExpressionStr(expression, sb);
		} else {
			List<ExpressionNode> children = expression.getChildren();
			if (children != null && children.size() > 0) {
				sb.append("(");
				if (expression.getType().equalsIgnoreCase(Constants.EXP_NOT))
					sb.append(Constants.EXP_NOT);
				makeExpressionStr(children.get(0), sb);
				for (int i = 1; i < children.size(); i++) {
					if (expression.getType()
							.equalsIgnoreCase(Constants.EXP_NOT))
						sb.append(" ");
					else
						sb.append(" " + expression.getType() + " ");
					makeExpressionStr(children.get(i), sb);
				}
				sb.append(")");
			}

		}
	}

	private static void makeExpressionSQLStr(ExpressionNode expression,
			StringBuffer sb, String tableAlias) {
		if (expression.getType().equalsIgnoreCase(Constants.EXP_BASE)) {
			makeSingleExpressionSQLStr(expression, sb, tableAlias);
		} else {
			List<ExpressionNode> children = expression.getChildren();
			if (children != null && children.size() > 0) {
				sb.append("(");
				if (expression.getType().equalsIgnoreCase(Constants.EXP_NOT))
					sb.append(Constants.EXP_NOT);
				boolean first = true;
				for (int i = 0; i < children.size(); i++) {
					ExpressionNode childNode = children.get(i);
					if (childNode.getType()
							.equalsIgnoreCase(Constants.EXP_BASE)
							&& Constants.sqlOperatorsMap
									.get(childNode.getOpr()) == null) {
						continue;
					}
					if (!first) {
						if (expression.getType().equalsIgnoreCase(
								Constants.EXP_NOT))
							sb.append(Constants.EXP_OR).append(" ")
									.append(Constants.EXP_NOT);
						else
							sb.append(" " + expression.getType() + " ");
					}
					makeExpressionSQLStr(children.get(i), sb, tableAlias);
					first = false;
				}
				sb.append(")");
			}
		}
	}

	/**
	 * 转换base运算的表达式为字符串
	 * 
	 * @param expression
	 * @param sb
	 */
	private static void makeSingleExpressionStr(ExpressionNode expression,
			StringBuffer sb) {
		sb.append("(");
		makeVarBindsStr(expression.getLeft(), sb);
		sb.append(" " + expression.getOpr() + " ");
		makeVarBindsStr(expression.getRight(), sb);
		sb.append(")");
	}

	private static void makeSingleExpressionSQLStr(ExpressionNode expression,
			StringBuffer sb, String tableAlias) {
		String opr = expression.getOpr();
		String sqlOpr = Constants.sqlOperatorsMap.get(opr);

		sb.append("(");

		if (tableAlias != null) {
			sb.append(tableAlias + ".");
		}
		if (sqlOpr.equals("")) {
			if (opr.equalsIgnoreCase(Constants.OPR_BETWEEN)) {
				makeVarBindsStr(expression.getLeft(), sb);
				sb.append(" between ");
				sb.append(makeValueSQLStr(expression.getRight().getValues()[0],
						false)
						+ " and "
						+ makeValueSQLStr(expression.getRight().getValues()[1],
								false));
			} else if (opr.equalsIgnoreCase(Constants.OPR_ISNULL)) {
				makeVarBindsStr(expression.getLeft(), sb);
				if (isIPAddr(expression)) {
					/**
					 * Modify by liyue 2012-9-20
					 * 由于地址类型改为byte[]，因此对地址类型进行查询的时候，判断地址是否为空，需要用is null或者is not
					 * null
					 */
					if (expression.getRight().getValue()
							.equalsIgnoreCase("yes"))
						// sb.append(" = -1");
						sb.append(" is null ");
					else
						// sb.append(" <> -1");
						sb.append(" is not null ");
				} else {
					if (expression.getRight().getValue()
							.equalsIgnoreCase("yes")) {
						sb.append(" is null ");
					} else {
						sb.append(" is not null");
					}
				}
			} else if (opr.equalsIgnoreCase(Constants.OPR_STARTSWITH)) {
				makeVarBindsStr(expression.getLeft(), sb);
				if (isIPAddr(expression) && expression.getRight() != null) {
					// long[] ip = analyseIP(expression.getRight().getValue());
					// sb.append(" between ").append(ip[0]).append(" and ").append(ip[1]);
					// sb.append(" ").append(">=").append(IPTools.ipToLong(expression.getRight().getValue())).append(" ");
					/**
					 * Modify by liyue 2012-9-20 由于地址类型改为byte[]，因此对地址类型进行查询的时候，
					 * 需要将输入的string型地址使用inet6_aton来转换为byte[]型
					 */
					sb.append(" ")
							.append(">=")
							.append(" inet6_aton('"
									+ expression.getRight().getValue() + "') ")
							.append(" ");
				} else {
					/**
					 * Modify by liyue 2012-11-19
					 * 由于设备类型改为int，因此对设备类型的查询需要修改，将原来的'like /1'修改为'in (1,99)'
					 */
					if (isDevType(expression) && expression.getRight() != null) {
						// 1、得到要查询的设备类型
						String rightStr = expression.getRight().getValue();
						// 2、根据设备类型，确定查询的具体范围
						String devTypeConditionStr = getDevTypeCondition(rightStr);
						// 3、组装sql语句
						if (devTypeConditionStr != null
								&& !"".equals(devTypeConditionStr.trim())) {
							sb.append(" between " + devTypeConditionStr + " ");
						}
					} else {
						sb.append(" like '" + expression.getRight().getValue()
								+ "%'");
					}
				}
			} else if (opr.equalsIgnoreCase(Constants.OPR_ENDSWITH)) {
				makeVarBindsStr(expression.getLeft(), sb);
				if (isIPAddr(expression))
					// sb.append(" ").append("<=").append(IPTools.ipToLong(expression.getRight().getValue())).append(" ");
					sb.append(" ")
							.append("<=")
							.append(" inet6_aton('"
									+ expression.getRight().getValue() + "') ")
							.append(" ");
				else
					sb.append(" like '%" + expression.getRight().getValue()
							+ "'");
			} else if (opr.equalsIgnoreCase(Constants.OPR_CONTAINS)
					&& !isIPAddr(expression)) {
				makeVarBindsStr(expression.getLeft(), sb);
				sb.append(" like '%" + expression.getRight().getValue() + "%'");
			}
		} else {
			if (isSpecialTime(expression)) {
				makeVarBindsSpecialTime(expression, sb, tableAlias);
			} else {
				makeVarBindsStr(expression.getLeft(), sb);
				sb.append(" " + sqlOpr + " ");
				if (isIPAddr(expression) && expression.getRight() != null
						&& expression.getRight().getValues() != null) {
					VarBinds temp = new VarBinds();
					String[] values = new String[expression.getRight()
							.getValues().length];
					for (int i = 0; i < values.length; i++) {
						String value = expression.getRight().getValues()[i];
						/**
						 * Modify by liyue 2012-9-20
						 * 由于地址类型改为byte[]，因此对地址类型进行查询的时候
						 * ，需要将输入的string型地址使用inet6_aton来转换为byte[]型
						 */
						values[i] = String.valueOf(" inet6_aton('" + value
								+ "') ");
					}
					temp.setValues(values);
					makeVarBindsSQLStr(temp, sb, false, opr);
				} else
					makeVarBindsSQLStr(expression.getRight(), sb,
							Constants.stringOperators.contains(expression
									.getOpr()), opr);
			}
		}
		sb.append(")");
	}

	/**
	 * 对时间字段特殊处理,当 操作符为 = : 转化为 >=value and <value+1000 != : 转化为 <value or
	 * >=value+1000 >= : 转化为 >= value(不做转化) <= : 转化为 < value+1000 > : 转化为 >=
	 * value + 1000 < : 转化为<= value - 1000 属于(innumber) : 转化为 >=value[0] and
	 * <value[0]+1000 or >=value[1] and < value[1]+1000 and ...
	 */
	private static StringBuffer makeVarBindsSpecialTime(
			ExpressionNode expression, StringBuffer sb, String tableAlias) {
		StringBuffer sbLeft = new StringBuffer();
		String sqlOprLT = Constants.sqlOperatorsMap.get(Constants.OPR_LT);
		String sqlOprLE = Constants.sqlOperatorsMap.get(Constants.OPR_LE);
		// String sqlOprGT = Constants.sqlOperatorsMap.get(Constants.OPR_GT);
		String sqlOprGE = Constants.sqlOperatorsMap.get(Constants.OPR_GE);
		String opr = expression.getOpr();

		if (tableAlias != null)
			sbLeft.append(tableAlias + ".");

		makeVarBindsStr(expression.getLeft(), sbLeft);

		long[] values = new long[expression.getRight().getValues().length];
		for (int i = 0; i < values.length; i++) {
			String value = expression.getRight().getValues()[i];
			values[i] = Long.valueOf(value);
		}
		if (opr.equals(Constants.OPR_EQ)) {
			sb.append(sbLeft).append(sqlOprGE).append(" ").append(values[0]);
			sb.append(" ").append(Constants.EXP_AND);
			sb.append(" ").append(sbLeft).append(sqlOprLT).append(" ")
					.append(values[0] + 1000);
		} else if (opr.equals(Constants.OPR_NE)) {
			sb.append(sbLeft).append(sqlOprLT).append(" ").append(values[0]);
			sb.append(" ").append(Constants.EXP_OR);
			sb.append(" ").append(sbLeft).append(sqlOprGE).append(" ")
					.append(values[0] + 1000);
		} else if (opr.equals(Constants.OPR_INNUMBER)) {
			boolean isFirst = true;
			for (long value : values) {
				if (!isFirst)
					sb.append(" ").append(Constants.EXP_OR);
				sb.append("(").append(sbLeft).append(sqlOprGE).append(" ")
						.append(value);
				sb.append(" ").append(Constants.EXP_AND);
				sb.append(" ").append(sbLeft).append(sqlOprLT).append(" ")
						.append(value + 1000);
				sb.append(")");
				isFirst = false;
			}
			/*
			 * }else if(opr.equals(Constants.OPR_GE)){
			 * sb.append(sbLeft).append(sqlOprGT
			 * ).append(" ").append(values[0]-1000);
			 */} else if (opr.equals(Constants.OPR_LE)) {
			sb.append(sbLeft).append(sqlOprLT).append(" ")
					.append(values[0] + 1000);
		} else if (opr.equals(Constants.OPR_GT)) {
			sb.append(sbLeft).append(sqlOprGE).append(" ")
					.append(values[0] + 1000);
		} else if (opr.equals(Constants.OPR_LT)) {
			sb.append(sbLeft).append(sqlOprLE).append(" ")
					.append(values[0] - 1000);
		}
		return sb;
	}

	/**
	 * 将操作书转换为表达式
	 * 
	 * @param var
	 * @param sb
	 */
	private static void makeVarBindsStr(VarBinds var, StringBuffer sb) {
		if (var == null)
			return;
		if (var.getAttrname() != null)
			sb.append(var.getAttrname());
		if (var.getRef() != null)
			sb.append(var.getRef());
		if (var.getValues() != null) {
			String[] values = var.getValues();
			if (values.length >= 1) {
				if (values.length == 1) {
					sb.append(values[0]);
				} else {
					sb.append("(");
					sb.append(values[0]);
					for (int i = 1; i < values.length; i++) {
						sb.append(",");
						sb.append(values[i]);
					}
					sb.append(")");
				}
			}

		}
	}

	private static String makeValueSQLStr(String value, boolean isStr) {
		String result = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		if (isStr) {
			result = "'" + value + "'";
		} else {
			try {
				Integer.parseInt(value);
				result = value;
			} catch (NumberFormatException e) {
				try {
					result = String.valueOf(sdf.parse(value).getTime());
				} catch (ParseException e1) {
					result = value;
				}
			}
		}
		return result;
	}

	private static void makeVarBindsSQLStr(VarBinds var, StringBuffer sb,
			boolean isStr) {
		makeVarBindsSQLStr(var, sb, isStr, null);
	}

	private static void makeVarBindsSQLStr(VarBinds var, StringBuffer sb,
			boolean isStr, String opr) {
		if (var == null)
			return;
		if (var.getAttrname() != null)
			sb.append(var.getAttrname());
		if (var.getValues() != null) {
			String[] values = var.getValues();
			if (values.length >= 1) {
				if (values.length == 1) {
					if (opr != null
							&& (Constants.OPR_IN.equals(opr) || Constants.OPR_INNUMBER
									.equals(opr)))
						sb.append("(")
								.append(makeValueSQLStr(values[0], isStr))
								.append(")");
					else
						sb.append(makeValueSQLStr(values[0], isStr));
				} else {
					sb.append("(");
					for (int i = 0; i < values.length; i++) {
						if (i > 0) {
							sb.append(",");
						}
						sb.append(makeValueSQLStr(values[i], isStr));
					}
					sb.append(")");
				}
			}

		}
	}

	public static void reflectEventObject() {

		Class eventClass = SIMEventObject.class;
		long s1 = System.currentTimeMillis();
		Field[] fields = eventClass.getDeclaredFields();
		long s2 = System.currentTimeMillis();
		for (int i = 0; i < fields.length; i++) {
			System.out.println(fields[i].getName().toLowerCase());
		}

		SIMEventObject event = new SIMEventObject();
		// event.setAction("adfaf");
		event.setAction(1); // 新的事件属性字段，Action为字典表，类型为int， 1：创建
		event.setID(999);
		event.setDAddr("10.60.60.3");

		System.out.println("-------------------------------------");
		Map map = new HashMap();
		long s3 = System.currentTimeMillis();
		try {
			for (int i = 0; i < fields.length; i++) {
				// System.out.println(fields[i].get((Object)event));
				map.put(fields[i].getName().toLowerCase(),
						fields[i].get((Object) event));
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		long s4 = System.currentTimeMillis();

		System.out.println("elapse:" + (s2 - s1) + " " + (s4 - s3));

	}

	/**
	 * 复制SIMEventObject
	 * 
	 * @param src
	 * @return
	 */
	public static SIMEventObject copy(SIMEventObject src) {
		if (src == null)
			return null;
		try {
			return (SIMEventObject) src.clone();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * 复制bean对象
	 * 
	 * @param bean
	 * @return
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public static Object cloneBean(Object bean) throws IllegalAccessException,
			InstantiationException, InvocationTargetException,
			NoSuchMethodException {
		Class clazz = bean.getClass();
		Object newBean = clazz.newInstance();
		PropertyUtils.copyProperties(newBean, bean);
		return newBean;
	}

	public static void main(String[] args) {
		// reflectEventObject();
		// loadFiltersFromDB();
		// loadRulesFromDB();

		/*
		 * SIMEventObject event = new SIMEventObject(); event.setRawID(9999L);
		 * 
		 * Map map = new HashMap(); System.out.println(event); long a =
		 * System.currentTimeMillis(); try { for (int i = 0; i < 60000; i++) {
		 * // SIMEventObject disc = copy(event); //
		 * System.out.println(disc.getRawID());
		 * FilterAssistant.prepareParameters(event,map); } } catch (Exception e)
		 * { // TODO: handle exception } long b = System.currentTimeMillis();
		 * System.out.println(b-a);
		 */

		// String resouceStr = "!(D: Y 0 M 5 D 31 10:30:33 - Y 0 M 5 D 31
		// 10:30:33,!D: Y 0 M 5 D 31 10:30:33 - Y 2003 M 5 D 31 10:30:33)";
		// TimeResource prp = transFromStringToTimeResource(resouceStr);
		// System.out.println(transFromTimeResourceToString(prp));
		// ExpressionNode en = constructExpressionNode("devaddr in (1.1.1.1 ,
		// 2.2.2.2)");
		// constructExpressionNode("devaddr eq_str 10.50.10.50");
	}
}

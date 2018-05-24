package org.opennms.netmgt.syslogd.analyze;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;
import org.opennms.netmgt.syslogd.parse.SIMEventObject;

public class FilterDelegateImpl implements FilterDelegate {
	/**
	 * 日志接口
	 */
	private static Logger logger = Logger.getLogger(FilterDelegateImpl.class);

	/**
	 * 时间类型的属性名字集合
	 */
	private static Set<String> timeStrSet = new HashSet<String>();
	static {
		timeStrSet.add("recepttime");
		timeStrSet.add("sendtime");
		timeStrSet.add("occurtime");
		timeStrSet.add("duration");
	}

	/**
	 * 时间格式串，必须与用户界面编辑或xml文件中的格式一致。
	 */
	public static String TIMEFORMAT = "yyyy-MM-dd HH:mm:ss";

	/**
	 * 过滤器对象。
	 */
	private SIMFilterObject filter;

	/**
	 * 构造函数。
	 * 
	 * @param filter
	 */
	public FilterDelegateImpl(SIMFilterObject filter) {
		if (filter == null)
			throw new NullPointerException(
					"Filter object is null. Such delegate can not be constructed!");
		format(filter);
		this.filter = filter;
	}

	/**
	 * 获得过滤器对象。
	 */
	public SIMFilterObject getFilterObject() {
		return filter;
	}

	public boolean isSatisfied(Object event) {
		// TODO Auto-generated method stub
		if (event == null)
			return false;
		return isSatisfiedInline(event);
	}

	public boolean isSatisfied(SIMEventObject event) {
		// TODO Auto-generated method stub
		if (event == null)
			return false;
		return isSatisfiedInline(event);
	}

	public boolean isSatisfied(Map varMap) {
		if (varMap == null)
			return false;
		return isSatisfiedInline(varMap);
	}

	public boolean isSatisfied(List<SIMEventParameter> events) {
		if (events == null || events.size() == 0)
			return false;
		return isSatisfiedInline(events);
	}

	/**
	 * 对过滤器对象中的内容进行格式化。 1）对将时间类型的字符串表示转换为长整数 2) 可以在format中，把一些参数重新小写化一下 3)
	 * 对OPR_MATCHESFILTER操作补充一下右节点 4）对OPR_INSUBNET操作改造一下右节点变量
	 * 
	 * @param filter
	 */
	void format(SIMFilterObject filter) {
		// @@
		ExpressionNode root = filter.getRoot();
		if (root == null)
			return;

		Queue<ExpressionNode> queue = new LinkedList<ExpressionNode>();// 先进先出队列，采用树的宽度遍历算法，对叶子节点进行格式化。
		queue.add(root);

		do {
			ExpressionNode qNode = queue.poll();// 从队列头获取结点
			if (qNode == null)
				break;

			// format type
			if (qNode.getType() != null)
				qNode.setType(qNode.getType().toLowerCase());
			if (qNode.getType().equals("not") || qNode.getType().equals("and")
					|| qNode.getType().equals("or")) {// 非叶结点
				if (qNode.getChildren() != null) {
					List<ExpressionNode> list = qNode.getChildren();
					for (int i = 0; i < list.size(); i++) {
						queue.add(list.get(i));
					}
				}
			} else {// 叶结点
				// format opr
				if (qNode.getOpr() != null)
					qNode.setOpr(qNode.getOpr().toLowerCase());

				if (qNode.getOpr().equals(Constants.OPR_MATCHESFILTER)
						|| qNode.getOpr().equals(
								Constants.OPR_MATCHESFILTERPLUGIN)
						|| qNode.getOpr().equals(Constants.OPR_MATCHESBWLIST)) {// 如果是过滤器匹配类型或过滤器插件类型或黑白名单类型，则将左右节点设置为同一个，这个在计算时取右操作数时才不为空
					if (qNode.getRight() == null)
						qNode.setRight(qNode.getLeft());
				}

				if (qNode.getOpr().equals(Constants.OPR_INSUBNET)) {// 如果是"属于子网"类型，则将右节点的values数组，转化为IPResource对象，并存于右节点VarBinds的internValueObject成员中,并改变VarBinds中的type为"intern"。
					formatIPResource(qNode.getRight());
				}

				formatString(qNode.getLeft());
				formatString(qNode.getRight());
				/**
				 * if (!formatTime(qNode.getLeft(), qNode.getRight()))
				 * formatTime(qNode.getRight(), qNode.getLeft());
				 */
				formatTime(qNode);

				if (qNode.getOpr().equals(Constants.OPR_MATCHESRULE)) {// 如果是"触发告警"类型，则改变该节点，扩充为一棵子树。
					formatMatchesRuleNode(qNode);
				}
			}

		} while (true);
	}

	/**
	 * 将OPR_MATCHESRULE的操作节点替换为(systype eq 2 and msg startswith rule=???;)的节点
	 * 
	 * @param qNode
	 */
	void formatMatchesRuleNode(ExpressionNode qNode) {

		// 获得规则id
		String ruleid = qNode.getLeft().getRef();

		// 创建运算叶节点typeNode
		ExpressionNode typeNode = new ExpressionNode();// systype eq 2的运算节点
		VarBinds sysTypeNameVar = new VarBinds();// typeNode的左var节点
		sysTypeNameVar.setType(Constants.VAR_EVENT);
		sysTypeNameVar.setAttrname("systype");
		VarBinds sysTypeValueVar = new VarBinds();// typeNode的右var节点
		sysTypeValueVar.setType(Constants.VAR_CONSTANT);
		sysTypeValueVar.setValue("2");
		typeNode.setType(Constants.EXP_BASE);// 构造typeNode节点
		typeNode.setOpr(Constants.OPR_EQ);
		typeNode.setLeft(sysTypeNameVar);
		typeNode.setRight(sysTypeValueVar);

		// 创建运算叶节点ruleNode
		ExpressionNode ruleNode = new ExpressionNode();// msg startswith
														// rule=xxxx;的运算节点
		VarBinds msgNameVar = new VarBinds();// typeNode的左var节点
		msgNameVar.setType(Constants.VAR_EVENT);
		msgNameVar.setAttrname("msg");
		VarBinds msgeValueVar = new VarBinds();// typeNode的右var节点
		msgeValueVar.setType(Constants.VAR_CONSTANT);
		msgeValueVar.setValue("rule=" + ruleid + ";");
		ruleNode.setType(Constants.EXP_BASE);// 构造ruleNode节点
		ruleNode.setOpr(Constants.OPR_STARTSWITH);
		ruleNode.setLeft(msgNameVar);
		ruleNode.setRight(msgeValueVar);

		// 更改OPR_MATCHESRULE节点
		List<ExpressionNode> list = new ArrayList<ExpressionNode>();
		list.add(typeNode);
		list.add(ruleNode);
		qNode.setType(Constants.EXP_AND);
		qNode.setChildren(list);
	}

	/**
	 * 将参数的属性名称小写
	 * 
	 * @param one
	 */
	void formatString(VarBinds one) {
		if (one != null && one.getAttrname() != null)
			one.setAttrname(one.getAttrname().toLowerCase());
	}

	/**
	 * 将属于子网操作的右操作数中的values数组转为为IPResource对象，
	 * 并保存于VarBinds的internValueObject中,并改变VarBinds中的type为"intern"
	 * 
	 * @param right
	 */
	void formatIPResource(VarBinds right) {
		if (right == null)
			return;
		if (right.getValues() == null || right.getValues().length == 0)
			return;

		String[] values = right.getValues();
		StringBuffer sb = new StringBuffer();
		sb.append("\"" + values[0] + "\"");
		if (values.length > 1) {
			sb.insert(0, "(");
			for (int i = 1; i < values.length; i++) {
				sb.append(",\"" + values[i] + "\"");
			}
			sb.append(")");
		}
		IPResource ipResource = TransformHelper.transFromStringToIPResource(sb
				.toString());
		right.setType(Constants.VAR_INTERN);
		right.setInternValueObject(ipResource);
	}

	/**
	 * 根据两变量的情况格式化时间字符串
	 * 
	 * @param one
	 * @param two
	 */
	boolean formatTime(VarBinds one, VarBinds two) {
		if (one != null && timeStrSet.contains(one.getAttrname())) {// one中识别出时间类型的属性
			if (two != null && two.getType().equalsIgnoreCase("constant")) {
				String[] timeStrArray = two.getValues();
				two.setValues(transToLongFromTimeStr(timeStrArray));
				return true;
			}
		}
		return false;
	}

	/**
	 * 根据结点格式化时间字符串,如果操作符是 isNull,则不需要格式化
	 * 
	 * @param node
	 *            结点
	 * @see formatTime(VarBinds one,VarBinds two)
	 * @return
	 */
	void formatTime(ExpressionNode node) {
		VarBinds left = node.getLeft(), right = node.getRight();
		String opr = node.getOpr();
		if (!opr.equals(Constants.OPR_ISNULL)) {
			if (!formatTime(left, right))
				formatTime(right, left);
		}
	}

	/**
	 * 将一组时间字符串转换为长整数。认为该时间字符串都具有：yyyy-MM-dd HH:mm:ss的格式
	 * 
	 * @param timeStrArray
	 * @return
	 */
	String[] transToLongFromTimeStr(String[] timeStrArray) {
		if (timeStrArray == null)
			return null;
		String[] result = new String[timeStrArray.length];
		SimpleDateFormat theSDF = new SimpleDateFormat(TIMEFORMAT);
		Calendar c = Calendar.getInstance();
		try {
			for (int i = 0; i < result.length; i++) {
				c.setTime(theSDF.parse(timeStrArray[i]));
				result[i] = String.valueOf(c.getTimeInMillis());
			}
		} catch (Exception e) {
			logger.error(
					"filter varbinds transformed from time string to long error",
					e);
		}

		return result;
	}

	/**
	 * 内部使用的判断方法。
	 * 
	 * @param parameters
	 *            传入的需要进行判断的实际事件参数
	 * @return
	 */
	boolean isSatisfiedInline(Object parameters) {

		ExpressionNode root = filter.getRoot();
		try {
			return calAll(root, parameters);
		} catch (IllegalAccessException e) {
			logger.error("Can not get event attribute value:" + e.getMessage());
			return false;
		}

	}

	/**
	 * = 计算逻辑表达式树，返回布尔值
	 * 
	 * @param curr
	 *            表达式树的顶级结点。
	 * @param parameters
	 *            , 传入的需要进行判断的实际事件参数
	 *            可以是Map类型，表示变量与变量对应的值组成的map。作为逻辑表达式从外界输入的值。也就是实参表。
	 *            也可以是List类型，表示多个事件变量。
	 * @return
	 */
	boolean calAll(ExpressionNode curr, Object parameters)
			throws IllegalAccessException {
		// @@考虑每一个可能潜在不规范的地方！
		if (curr == null) {
			// throw new NullPointerException(
			// "ExpressionNode is null. Can not be judged in filter:"
			// + filter.getId() + " " + filter.getName() + "!");
			logger.info("Expression root is null. Can not be judged in filter:"
					+ filter.getId() + " " + filter.getName() + "!");
			return false;
		}

		if (curr.getType().equals("base")) {// 叶子结点，直接计算
			return calBase(curr.getLeft(), curr.getOpr(), curr.getRight(),
					parameters);
		} else {// 非叶结点，递归计算
			List<ExpressionNode> children = curr.getChildren();
			if (children == null || children.size() == 0) {
				logger.info("Children expression node has no value when need it. Can not be judged in filter:"
						+ filter.getId()
						+ " "
						+ filter.getName()
						+ " expressionnode:" + curr.getType());
				return false;
				// throw new NullPointerException(
				// "Children expression node has no value when need it. Can not
				// be judged in filter:"
				// + filter.getId()
				// + " "
				// + filter.getName()
				// + " expressionnode:" + curr.getType());
			}

			if (curr.getType().equals("and")) {
				for (int i = 0; i < children.size(); i++) {
					boolean isOK = calAll(children.get(i), parameters);
					if (!isOK)// 孩子表达式中只要有一个返回假，则该and的表达式返回假
						return false;
				}
				return true;// 孩子表达式全部返回真，则该and的表达式返回真

			} else if (curr.getType().equals("or")) {
				for (int i = 0; i < children.size(); i++) {
					boolean isOK = calAll(children.get(i), parameters);
					if (isOK)// 孩子表达式中只要有一个返回真，则该or的表达式返回真
						return true;
				}
				return false;// 孩子表达式全部返回假，则该or的表达式返回假

			} else if (curr.getType().equals("not")) {
				/*
				 * if (children.size() > 1){ logger.info( filter.getId() + " " +
				 * filter.getName()+" error logic type:" + curr.getType() + " it
				 * should has only one child expression!. System will use the
				 * first one as input"); } return !calAll(children.get(0),
				 * parameters);
				 */
				// 修改not的策略：
				boolean result = false;
				for (int i = 0; i < children.size(); i++) {
					boolean isOK = calAll(children.get(i), parameters);
					if (isOK) {// not的孩子按照or的行为进行判断
						result = true;
						break;
					}
				}
				return !result;// 对孩子结果取反。

			} else {
				throw new IllegalArgumentException("error logic type:"
						+ curr.getType()
						+ " it should be one of the 'not' 'and' 'or' 'base'!");
			}
		}

	}

	/**
	 * 计算逻辑表达式树的叶结点。该步骤将过滤器中指定的参数替换为传入的实际参数，然后进行计算。
	 * 
	 * @param left
	 *            表达式左侧符号
	 * @param opr
	 *            表达式操作符
	 * @param right
	 *            表达式右侧符号
	 * @param parameters
	 *            外界输入的用于比较判断的值
	 * @return
	 */
	boolean calBase(VarBinds left, String opr, VarBinds right, Object parameters)
			throws IllegalAccessException {
		/**
		 * Add by liyue 2013-1-28 左侧符号为地址类标识
		 */
		boolean needAddressTrans = false;
		/**
		 * Add by liyue 2013-1-28
		 * 当左侧varBind的type为事件变量，且字段为collectorAddr,sAddr,stAddr
		 * ,dAddr,dtAddr,devAddr时，右侧的值需要进行InetAddress转化，以支持IPv6地址
		 */
		if (left != null && Constants.VAR_EVENT.equals(left.getType())) {
			if (Constants.addressFieldSet.contains(left.getAttrname())) {
				needAddressTrans = true;
			}
		}
		// -- 提取left与right对应变量的值 --
		Object leftOperand = FilterAssistant.getLeftOperand(left, parameters);
		Object rightOperand = FilterAssistant.getRightOperand(right, opr,
				parameters, needAddressTrans);
		// -- 计算 --
		return FilterAssistant.cal(leftOperand, opr, rightOperand);
	}

	public static void main(String[] args) {

		// SIMFilterObject filter = new SIMFilterObject();// 包含表达式的过滤器对象
		// Map varMap = new HashMap();// 输入变量
		//
		// FilterDelegateImpl f = new FilterDelegateImpl(filter);
		// System.out.println(f.isSatisfied(varMap));
		Calendar c = Calendar.getInstance();
		System.out.println(c.getTimeInMillis() + " "
				+ System.currentTimeMillis());
		System.out.println(new Date(1157527560000L));
	}

}

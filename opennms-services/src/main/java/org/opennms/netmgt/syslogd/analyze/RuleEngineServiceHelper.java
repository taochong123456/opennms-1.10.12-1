package org.opennms.netmgt.syslogd.analyze;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.syslogd.logic.IncategoryLogic;
import org.opennms.netmgt.syslogd.logic.MatchesAddrResource;
import org.opennms.netmgt.syslogd.logic.MatchesPortResourceLogic;
import org.opennms.netmgt.syslogd.logic.MatchesTimeResourceLogic;
import org.opennms.netmgt.syslogd.parse.SIMEventObject;

public class RuleEngineServiceHelper {

	/**
	 * 日志接口。
	 */
	private static Logger logger = Logger
			.getLogger(RuleEngineServiceHelper.class);

	/**
	 * 规则引擎服务实例
	 */
	private static RuleEngineService service;

	/**
	 * 事件缓冲管理器
	 */
	private static EventBufferManager eventBufferManager;

	/**
	 * 规则引擎服务的名字，应该对应于sysconfig.xml中设置的规则服务的名字
	 */
	private static String serviceName = "RuleEngineService";

	/**
	 * 获得规则引擎服务的实例。
	 * 
	 * @return
	 */
	public static RuleEngineService getService() {

		return service;
	}

	/**
	 * 获得事件缓存管理器实例
	 * 
	 * @return
	 */
	public static EventBufferManager getEventBufferManager() {
		if (eventBufferManager == null) {
			RuleEngineService myService = getService();
			if (myService != null) {
				eventBufferManager = myService.getEventBufferManager();
			}
		}
		return eventBufferManager;
	}

	/**
	 * 获得规则引擎服务的名字。
	 * 
	 * @return
	 */
	public static String getServiceName() {
		return serviceName;
	}

	/**
	 * 设置规则引擎服务器的名字。
	 * 
	 * @param serviceName
	 */
	public static void setServiceName(String serviceName) {
		RuleEngineServiceHelper.serviceName = serviceName;
	}

	/**
	 * 向内存中添加过滤器。
	 * 
	 * @param filter
	 * @return
	 */
	public static boolean addFilter(SIMFilterObject filter) {
		RuleEngineService myService = getService();
		if (myService != null)
			return myService.getFilterManager().addFilter(filter);
		else
			return false;
	}

	/**
	 * 更新内存中的过滤器
	 * 
	 * @param filter
	 * @return
	 */
	public static boolean updateFilter(SIMFilterObject filter) {
		RuleEngineService myService = getService();
		if (myService != null)
			return myService.getFilterManager().updateFilter(filter);
		else
			return false;
	}

	/**
	 * 删除内存中的过滤器
	 * 
	 * @param filterID
	 * @return
	 */
	public static boolean removeFilter(String filterID) {
		RuleEngineService myService = getService();
		if (myService != null)
			return myService.getFilterManager().removeFilter(filterID);
		else
			return false;
	}

	/**
	 * 获取指定filterid的过滤器代理对象
	 * 
	 * @param filterID
	 * @return
	 */
	public static FilterDelegate getFilterDelegate(String filterID) {
		RuleEngineService myService = getService();
		if (myService != null)
			return myService.getFilterManager().getFilterDelegate(filterID);
		else
			return null;
	}

	/**
	 * 计算临时过滤器的id，通常根据原先过滤器和新条件共同确定临时过滤器的id
	 * 
	 * @param originFilterID
	 * @param conditions
	 * @return
	 */
	public static String calculateTempFilterID(String originFilterID,
			String[] conditions) {
		StringBuffer sb = new StringBuffer();
		sb.append(originFilterID);
		if (conditions != null) {
			for (int i = 0; i < conditions.length; i++) {
				sb.append(conditions[i]);
			}
		}
		int hashCode = sb.toString().hashCode();
		return String.valueOf(hashCode);
	}

	/**
	 * 在源过滤器的基础上创建新的临时过滤器。
	 * 
	 * @param originFilterID
	 *            源过滤器id
	 * @param conditions
	 * @return
	 */
	public static String constructTempFilter(String originFilterID,
			String[] conditions) {
		// 计算临时过滤器的id
		String newFilterID = Constants.TEMP_FILTERID_PREFIX
				+ calculateTempFilterID(originFilterID, conditions);
		// 判断该过滤器是否已经存在，如果存在则直接返回此过滤器id
		RuleEngineService myService = getService();
		if (myService != null) {
			if (myService.getFilterManager().getFilterDelegate(newFilterID) != null)
				return newFilterID;
		}

		// 创建临时过滤器的根表达式
		ExpressionNode root = new ExpressionNode();
		root.setType(Constants.EXP_AND);
		List<ExpressionNode> list = new ArrayList<ExpressionNode>();
		root.setChildren(list);

		// 创建引用原过滤器的表达式
		if (originFilterID != null && originFilterID.trim().length() != 0) {
			ExpressionNode originFilterNode = new ExpressionNode();
			originFilterNode.setType(Constants.EXP_BASE);
			originFilterNode.setOpr(Constants.OPR_MATCHESFILTER);
			VarBinds left = new VarBinds();
			left.setType(Constants.VAR_FILTER);
			left.setRef(originFilterID);
			originFilterNode.setLeft(left);
			// originFilterNode.setRight(left);

			list.add(originFilterNode);
		}

		// 创建新条件组成的表达式
		if (conditions != null) {
			for (int i = 0; i < conditions.length; i++) {
				ExpressionNode condNode = TransformHelper
						.constructExpressionNode(conditions[i]);
				if (condNode != null)
					list.add(condNode);
			}
		}

		// 创建临时过滤器
		SIMFilterObject filter = new SIMFilterObject();
		filter.setId(newFilterID);
		filter.setRoot(root);

		// $$temp
		// System.out.println(TransformHelper.transExpressionNodeToString(root));

		// 加入到过滤器管理器中
		if (myService != null)
			myService.getFilterManager().addFilter(filter);

		return newFilterID;
	}

	public static String constructTempFilterByExpressionNode(
			String originFilterID, ExpressionNode node) {
		String nodeExp = TransformHelper.transExpressionNodeToString(node);
		String newFilterID = Constants.TEMP_FILTERID_PREFIX
				+ nodeExp.hashCode();
		RuleEngineService myService = RuleEngineServiceHelper.getService();
		if (myService != null
				&& myService.getFilterManager().getFilterDelegate(newFilterID) != null) {
			return newFilterID;
		}
		ExpressionNode root = new ExpressionNode();
		root.setType(Constants.EXP_AND);
		List<ExpressionNode> list = new ArrayList<ExpressionNode>();
		root.setChildren(list);
		if (originFilterID != null && originFilterID.trim().length() != 0) {
			ExpressionNode originFilterNode = new ExpressionNode();
			originFilterNode.setType(Constants.EXP_BASE);
			originFilterNode.setOpr(Constants.OPR_MATCHESFILTER);
			VarBinds left = new VarBinds();
			left.setType(Constants.VAR_FILTER);
			left.setRef(originFilterID);
			originFilterNode.setLeft(left);
			list.add(originFilterNode);
		}
		list.add(node);

		SIMFilterObject filter = new SIMFilterObject();
		filter.setId(newFilterID);
		filter.setRoot(root);
		if (myService != null) {
			myService.getFilterManager().addFilter(filter);
		}
		return newFilterID;
	}

	/**
	 * 根据过滤器id创建过滤器中含有的表达式的字符串表示。
	 * 
	 * @param filterID
	 * @return
	 */
	public static String constructFilterString(String filterID) {
		FilterDelegate fd = getFilterDelegate(filterID);
		if (fd != null) {
			return TransformHelper.transExpressionNodeToString(fd
					.getFilterObject().getRoot());
		} else
			return "";
	}

	/**
	 * 向内存中添加规则
	 * 
	 * @param rule
	 * @return
	 */
	public static boolean addRule(SIMRuleObject rule) {
		RuleEngineService myService = getService();
		if (myService != null)
			return myService.getRuleManager().addRule(rule);
		else
			return false;
	}

	/**
	 * 删除内存中的规则
	 * 
	 * @param ruleID
	 * @return
	 */
	public static boolean removeRule(String ruleID) {
		RuleEngineService myService = getService();
		if (myService != null)
			return myService.getRuleManager().removeRule(ruleID);
		else
			return false;
	}

	/**
	 * 更新内存中的规则
	 * 
	 * @param rule
	 * @return
	 */
	public static boolean updateRule(SIMRuleObject rule) {
		RuleEngineService myService = getService();
		if (myService != null)
			return myService.getRuleManager().updateRule(rule);
		else
			return false;
	}

	/**
	 * 更新内存中指定规则的名字
	 * 
	 * @param ruleID
	 * @param newRuleName
	 * @return
	 */
	public static boolean updateRuleName(String ruleID, String newRuleName) {
		RuleEngineService myService = getService();
		if (myService != null)
			return myService.getRuleManager().updateRuleName(ruleID,
					newRuleName);
		else
			return false;
	}

	/**
	 * 更新内存中指定规则的使能状态
	 * 
	 * @param ruleID
	 * @param enabled
	 * @return
	 */
	public static boolean updateRuleEnableStatus(String ruleID, boolean enabled) {
		RuleEngineService myService = getService();
		if (myService != null)
			return myService.getRuleManager().updateRuleEnableStatus(ruleID,
					enabled);
		else
			return false;
	}

	/**
	 * 像缓存中添加事件对象。
	 * 
	 * @param event
	 * @return
	 */
	public static boolean addEvent(SIMEventObject event) {
		// RuleEngineService myService = getService();
		// if(myService!=null)
		// return myService.getEventBufferManager().addEvent(event);
		// else
		// return false;
		EventBufferManager evetBM = getEventBufferManager();
		if (evetBM != null)
			return evetBM.addEvent(event);
		else
			return false;
	}

	/**
	 * 更新attrPathIDRelationMap表，此情况在添加资产属性时发生。
	 * 
	 * @param attributeID
	 */
	public static void updateIncategoryLogic(long attributeID) {
		IncategoryLogic.updateIncategoryLogic(attributeID);
	}

	/**
	 * 更新资源属性
	 * 
	 * @param resourceType
	 *            资源类型
	 * @param id
	 *            资源id
	 * @param content
	 *            资源字符串
	 */
	public static void updateResoucreContent(int resourceType, String id,
			String content) {
		switch (resourceType) {
		case Constants.IP_RES_TYPE:// 更新地址资源
			MatchesAddrResource.updateResource(id, content);
			break;
		case Constants.PORT_RES_TYPE:// 更新端口资源
			MatchesPortResourceLogic.updateResource(id, content);
			break;
		case Constants.TIME_RES_TYPE:// 更新时间资源
			MatchesTimeResourceLogic.updateResource(id, content);
			break;
		default:
			break;
		}
	}

	/**
	 * 更新资源属性
	 * 
	 * @param resourceType
	 *            资源类型
	 * @param id
	 *            资源id
	 */
	public static void deleteResoucreContent(int resourceType, String id) {
		switch (resourceType) {
		case Constants.IP_RES_TYPE:// 更新地址资源
			break;
		}
	}

	/**
	 * 从数据库中加载过滤器
	 * 
	 * @return 返回过滤器的字符串集合。
	 */
	public static List loadFiltersFromDB() {
		Connection conn = null;
		PreparedStatement pst = null;
		try {
			conn = DataSourceFactory.getInstance().getConnection();
			List<List> filterStrs = new ArrayList<List>();
			pst = conn
					.prepareStatement("select name,Filter_condition from t_filter");
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				List<String> list = new ArrayList<String>();
				list.add(rs.getString("name"));
				list.add(rs.getString("Filter_condition"));
				filterStrs.add(list);
			}
			return filterStrs;
		} catch (SQLException e) {
			logger.error("load filters from db error", e);
		} finally {
			try {
				pst.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 从数据库中加载规则
	 * 
	 */
	public static List loadRulesFromDB() {
		Connection conn = null;
		try {
			conn = DataSourceFactory.getInstance().getConnection();
			List<List> ruleStrs = new ArrayList<List>();
			PreparedStatement pst = conn
					.prepareStatement("select name,enabled,rule_content from t_rule");
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				List list = new ArrayList();
				list.add(rs.getString("name"));
				list.add(rs.getInt("enabled"));
				list.add(rs.getString("rule_content"));
				ruleStrs.add(list);
			}
			return ruleStrs;
		} catch (SQLException e) {
			logger.error("load rules from db error", e);
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}

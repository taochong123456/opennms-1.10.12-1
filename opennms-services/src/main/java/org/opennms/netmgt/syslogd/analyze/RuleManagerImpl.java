package org.opennms.netmgt.syslogd.analyze;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class RuleManagerImpl implements RuleManager {

	/**
	 * 日志接口。
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * 启动本事件缓冲管理器的服务例程
	 */
	private RuleEngineService service;

	/**
	 * 将启动的线程数目.
	 */
	private int threadNumber = 3;

	/**
	 * 线程组.
	 */
	private List<CorrelationThread> threads = new ArrayList<CorrelationThread>();

	public RuleManagerImpl(RuleEngineService service) {
		this.service = service;
	}

	/**
	 * 获得实时应用区域的规则.这些规则都是可以被系统执行的. 本函数必须保证结果中，每一个delegate中的SIMRuleObject都不能为空。
	 */
	List<RuleRobot> getRealTimeRules() {

		try {
			logger.info("Prepare to initialize rules.");

			List list = RuleEngineServiceHelper.loadRulesFromDB();
			// System.out.println("-----------rule number:"+list.size());
			if (list != null && list.size() > 0) {

				List<RuleRobot> delegates = new ArrayList<RuleRobot>();

				for (int i = 0; i < list.size(); i++) {
					String ruleName = (String) ((List) list.get(i)).get(0);
					Number enabled = (Number) ((List) list.get(i)).get(1);
					String ruleXMLStr = (String) ((List) list.get(i)).get(2);

					if (!StringTools.hasContent(ruleXMLStr)) {
						logger.error("rule "
								+ ruleName
								+ " doesn't has content. Can not be initialized.");
						continue;
					}

					try {
						SIMRuleObject rule = TransformHelper
								.transFromXMLToRuleObject(ruleXMLStr);
						if (enabled.intValue() == 1)
							rule.setEnabled(true);
						else
							rule.setEnabled(false);

						delegates.add(new RuleRobotImpl(rule, false));// 从数据库初始化时，不需要建立新的库表
						logger.info("init rule " + rule.getRuleID() + " : "
								+ rule.getName() + " over.");

					} catch (Exception e) {
						logger.error("rule " + ruleName
								+ " initialized failure.", e);
					}
				}

				logger.info("initialize rules over.");
				return delegates;

			} else {
				logger.info("No rules needs to be initialized.");
			}
		} catch (Exception e) {
			logger.error("init rule error", e);
		}

		return null;
	}

	/**
	 * 将rule对象编译为规则代理
	 * 
	 * @param rule
	 * @return
	 */
	RuleRobot compile(SIMRuleObject rule) {
		if (rule != null)
			return new RuleRobotImpl(rule, true);
		return null;
	}

	/**
	 * 获得线程在threads中的索引。该线程包含的规则数目最少
	 * 
	 * @return
	 */
	int getIndexOfMinRuleNumber() {
		int threadIndex = -1;
		int temp = Integer.MAX_VALUE;
		for (int i = 0; i < threads.size(); i++) {
			CorrelationThread ct = threads.get(i);
			int ruleNumber = ct.getRuleNumber();
			if (ruleNumber < temp) {
				threadIndex = i;
				temp = ruleNumber;
			}
		}
		return threadIndex;
	}

	public void init() {
		// 读取配置文件中的规则线程数目
		try {
			String num = "1";
			if (num != null && !num.equals(""))
				threadNumber = Integer.parseInt(num);
		} catch (Exception e) {
			// TODO: handle exception
		}
		// -- 创建线程 --
		for (int i = 0; i < threadNumber; i++) {
			List<RuleRobot> workArea = new ArrayList<RuleRobot>();
			CorrelationThread ct = new CorrelationThread(service, workArea);
			ct.setName("correlation Thread " + i);
			threads.add(ct);
		}

		// -- 将所有规则读入内存，并编译成RuleDelegate对象 --
		List<RuleRobot> delegates = getRealTimeRules();

		// -- 将这些规则分组，并将这些规则分配到线程中去 --
		if (delegates != null) {
			int totalRuleNumber = delegates.size();
			for (int i = 0; i < totalRuleNumber; i++) {
				RuleRobot delegate = delegates.get(i);
				threads.get(i % threadNumber).addRuleDelegate(delegate);
			}
		}

		// -- 启动线程 --
		for (int i = 0; i < threads.size(); i++) {
			threads.get(i).start();
		}

	}

	public void release() {
		// @@
		// 停止所有线程，释放所有线程
	}

	/**
	 * 添加规则
	 * 
	 * @param rule
	 * @return
	 */
	public boolean addRule(SIMRuleObject rule) {
		if (rule == null)
			return false;

		// System.out.println("add :"+rule.getName()+" "+rule.isEnabled());

		// -- 编译 --
		RuleRobot delegate = compile(rule);

		// -- 找线程组中规则最少的 --
		int threadIndex = getIndexOfMinRuleNumber();

		// -- 将规则代理加入这一组 --
		if (threadIndex != -1) {
			CorrelationThread ct = threads.get(threadIndex);
			return ct.addRuleDelegate(delegate);
		} else
			return false;
	}

	/**
	 * 删除指定ID的规则
	 * 
	 * @param ruleID
	 * @return
	 */
	public boolean removeRule(String ruleID) {
		for (int i = 0; i < threads.size(); i++) {
			CorrelationThread ct = threads.get(i);
			// -- 尝试移除 --
			boolean success = ct.removeRuleRobot(ruleID);
			if (success)
				return success;
		}
		return false;
	}

	// /**
	// * 更新指定规则
	// *
	// * @param rule
	// * @return
	// */
	// public boolean updateRule(SIMRuleObject rule) {
	//
	// // -- 编译 --
	// RuleRobot delegate = compile(rule);
	//
	// if (delegate == null)
	// return false;
	//
	// for (int i = 0; i < threads.size(); i++) {
	// CorrelationThread ct = threads.get(i);
	// // -- 尝试更新 --
	// boolean success = ct.updateRuleRobot(delegate);
	// if (success)
	// return success;
	// }
	// return false;
	//
	// }
	/**
	 * 更新指定规则
	 * 
	 * @param rule
	 * @return
	 */
	public boolean updateRule(SIMRuleObject rule) {

		if (rule == null)
			return false;

		// System.out.println("update :"+rule.getName()+" "+rule.isEnabled());

		for (int i = 0; i < threads.size(); i++) {
			CorrelationThread ct = threads.get(i);
			// -- 尝试更新 --
			boolean success = ct.updateRuleRobot(rule);
			if (success) {
				logger.info("update rule " + rule.getRuleID()
						+ " successfully. enabled=" + rule.isEnabled());
				return success;
			}
		}
		logger.info("update rule " + rule.getRuleID()
				+ " fail. can not find rule.");
		return false;

	}

	/**
	 * 更新指定规则的规则名字
	 * 
	 * @param ruleID
	 * @param newRuleName
	 * @return
	 */
	public boolean updateRuleName(String ruleID, String newRuleName) {

		if (!StringTools.hasContent(ruleID))
			return false;

		for (int i = 0; i < threads.size(); i++) {
			CorrelationThread ct = threads.get(i);
			// -- 尝试更新 --
			boolean success = ct.updateRuleRobotName(ruleID, newRuleName);
			if (success)
				return success;
		}
		return false;
	}

	/**
	 * 设置指定规则是否启用
	 * 
	 * @param ruleID
	 * @param enabled
	 * @return
	 */
	public boolean updateRuleEnableStatus(String ruleID, boolean enabled) {
		if (!StringTools.hasContent(ruleID))
			return false;

		for (int i = 0; i < threads.size(); i++) {
			CorrelationThread ct = threads.get(i);
			// -- 尝试更新 --
			boolean success = ct.updateRuleRobotEnableStatus(ruleID, enabled);
			if (success) {
				logger.info("update rule " + ruleID + " successfully. enabled="
						+ enabled);
				return success;
			}
		}
		logger.info("update rule " + ruleID + " fail. can not find rule.");
		return false;
	}

	public static void main(String[] args) {

	}
}

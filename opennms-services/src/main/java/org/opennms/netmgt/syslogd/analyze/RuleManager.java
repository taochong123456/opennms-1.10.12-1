package org.opennms.netmgt.syslogd.analyze;

public interface RuleManager {
	/**
	 * 初始化
	 * 
	 */
	public void init();

	/**
	 * 释放资源
	 * 
	 */
	public void release();

	/**
	 * 向规则管理器添加规则
	 * 
	 * @param rule
	 * @return
	 */
	public boolean addRule(SIMRuleObject rule);

	/**
	 * 从规则管理其中移除指定ID的规则
	 * 
	 * @param ruleID
	 * @return
	 */
	public boolean removeRule(String ruleID);

	/**
	 * 更新指定规则
	 * 
	 * @param rule
	 * @return
	 */
	public boolean updateRule(SIMRuleObject rule);

	/**
	 * 更新指定规则的规则名字
	 * 
	 * @param ruleID
	 * @param newRuleName
	 * @return
	 */
	public boolean updateRuleName(String ruleID, String newRuleName);

	/**
	 * 设置指定规则是否启用
	 * 
	 * @param ruleID
	 * @param enabled
	 * @return
	 */
	public boolean updateRuleEnableStatus(String ruleID, boolean enabled);
}

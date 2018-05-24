package org.opennms.netmgt.syslogd.analyze;

public interface ActionExecutor {
	/**
	 * 执行具体动作方法。
	 * 
	 * @param params
	 *            告警动作参数，params[][0]为参数名，params[][1]为参数值。
	 * @param envp
	 *            告警动作环境变量。参见
	 *            {@link secfox.soc.manage.alert.AlertManager#getAlertEnvp(MAlertObject alert)
	 *            getAlertEnvp}
	 */
	void execute(String[][] params, String[][] envp);
}

package org.opennms.netmgt.syslogd.analyze;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

public class RuleEngineServiceImpl implements RuleEngineService {
	/**
	 * 日志接口。
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * 服务名称。
	 */
	private String serviceName = "Rule Engine Service";

	/**
	 * 过滤器管理器。
	 */
	private FilterManager filterManager;

	/**
	 * 规则管理器。
	 */
	private RuleManager ruleManager;

	/**
	 * 事件缓存管理器。
	 */
	private EventBufferManager bufferManager;

	/**
	 * 动作执行器
	 */
	private ActionController actionController;

	/**
	 * 服务是否活动的标志。
	 */
	private boolean active;

	/*
	 * 获得事件缓存管理器。
	 * 
	 * @see
	 * secfox.soc.manage.audit.rule.RuleEngineService#getEventBufferManager()
	 */
	public EventBufferManager getEventBufferManager() {
		return bufferManager;
	}

	/*
	 * 获得规则管理器。
	 * 
	 * @see secfox.soc.manage.audit.rule.RuleEngineService#getRuleManager()
	 */
	public RuleManager getRuleManager() {
		return ruleManager;
	}

	/*
	 * 获得过滤器管理器。
	 * 
	 * @see secfox.soc.manage.audit.rule.RuleEngineService#getFilterManager()
	 */
	public FilterManager getFilterManager() {
		return filterManager;
	}

	/*
	 * 获得服务目前的工作状态
	 * 
	 * @see secfox.soc.commons.common.Service#isActive()
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * 设置服务状态
	 * 
	 * @param active
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/*
	 * 停止当前服务。
	 * 
	 * @see secfox.soc.commons.common.Service#stop()
	 */
	public void stop() {
		active = false;
	}

	/*
	 * 获得服务的名字
	 * 
	 * @see secfox.soc.commons.common.Service#getName()
	 */
	public String getName() {
		return serviceName;
	}

	/*
	 * 设置服务的名字
	 * 
	 * @see secfox.soc.commons.common.Service#setName(java.lang.String)
	 */
	public void setName(String name) {
		serviceName = name;
	}

	/*
	 * 启动服务
	 * 
	 * @see secfox.soc.commons.common.Service#start()
	 */
	public boolean start() {
		// -- 创建并初始化过滤器管理器 --
		filterManager = FilterManagerFactory.getFilterManager();
		filterManager.init();

		// -- 创建并初始化规则管理器 --
		ruleManager = new RuleManagerImpl(this);
		ruleManager.init();
		active = true;
		return active;
	}

	public ActionController getActionController() {
		return actionController;
	}

	public void setActionController(ActionController actionController) {
		this.actionController = actionController;
	}

}

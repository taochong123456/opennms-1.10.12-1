package org.opennms.netmgt.syslogd.analyze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;
import org.opennms.netmgt.syslogd.parse.Processor;
import org.opennms.netmgt.syslogd.parse.SIMEventObject;

public class CorrelationThread extends Thread {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * 启动本线程的服务例程引用。
	 */
	private RuleEngineService service;

	/**
	 * 事件缓存管理器
	 */
	private EventBufferManager eventBufferManager;

	/**
	 * 当前处理的事件对像代理。
	 */
	private SIMEventProxy eventProxy;

	/**
	 * 当前处理的事件对像代理对应的事件参数
	 */
	private Map currentEventParams = new HashMap();

	/**
	 * 需要进行规则判断的规则集合区。该规则区域的内容由RuleManager决定和分配。
	 */
	private List<RuleRobot> workArea;

	/**
	 * 访问规则集合区的锁定。
	 */
	private final ReentrantReadWriteLock enterLock = new ReentrantReadWriteLock();

	/**
	 * 构造函数
	 * 
	 * @param service
	 * @param workArea
	 */
	public CorrelationThread(RuleEngineService service, List<RuleRobot> workArea) {
		this.service = service;
		this.eventBufferManager = service.getEventBufferManager();
		if (workArea != null)
			this.workArea = workArea;
		else
			this.workArea = new ArrayList<RuleRobot>();
	}

	/**
	 * 获得当前处理的规则数目
	 * 
	 * @return 规则数目.
	 */
	public int getRuleNumber() {
		return workArea.size();
	}

	public List<RuleRobot> getWorkArea() {
		return workArea;
	}

	public void setWorkArea(List<RuleRobot> workArea) {
		this.workArea = workArea;
	}

	/**
	 * 向当前规则区中添加规则代理
	 * 
	 * @param delegate
	 * @return
	 */
	public boolean addRuleDelegate(RuleRobot delegate) {
		if (delegate == null)
			return false;
		else {
			try {
				enterLock.writeLock().lock();
				workArea.add(delegate);
				return true;
			} finally {
				enterLock.writeLock().unlock();
			}
		}
	}

	/**
	 * 在规则集中移除指定id的规则
	 * 
	 * @param ruleID
	 * @return 成功删除返回true,没有该id对应的规则或其他情况返回false;
	 */
	public boolean removeRuleRobot(String ruleID) {
		try {
			enterLock.writeLock().lock();
			for (int i = 0; i < workArea.size(); i++) {
				RuleRobot delegate = workArea.get(i);
				if (delegate.getRuleObject().getRuleID().equals(ruleID)) {
					delegate.getRuleObject().setEnabled(false);// 先让规则停下来
					delegate.release();// 然后释放资源
					workArea.remove(i);// 从缓冲区清除
					System.out.println("removeRuleDelegate:" + ruleID);
					return true;
				}
			}
			return false;

		} finally {
			enterLock.writeLock().unlock();
		}
	}

	/**
	 * 在规则集中更新规则代理
	 * 
	 * @param ruleID
	 * @return
	 */
	public boolean updateRuleRobot(RuleRobot delegate) {
		try {
			enterLock.writeLock().lock();
			for (int i = 0; i < workArea.size(); i++) {
				RuleRobot oldDelegate = workArea.get(i);

				if (delegate.getRuleObject().getRuleID().equals(oldDelegate.getRuleObject().getRuleID())) {
					workArea.set(i, delegate);
					return true;
				}
			}
			return false;

		} finally {
			enterLock.writeLock().unlock();
		}
	}

	/**
	 * 在规则集中更新规则代理
	 * 
	 * @param ruleID
	 * @return
	 */
	public boolean updateRuleRobot(SIMRuleObject rule) {
		try {
			enterLock.writeLock().lock();
			for (int i = 0; i < workArea.size(); i++) {
				RuleRobot oldDelegate = workArea.get(i);

				if (rule.getRuleID().equals(oldDelegate.getRuleObject().getRuleID())) {
					oldDelegate.getRuleObject().setEnabled(false);// 先将原来的规则停下来
					// -- 编译 --
					RuleRobot delegate = new RuleRobotImpl(rule, true);// 构建新的规则
					workArea.set(i, delegate);
					return true;
				}
			}
			return false;

		} finally {
			enterLock.writeLock().unlock();
		}
	}

	/**
	 * 更新指定规则的名字
	 * 
	 * @param ruleID
	 * @param newRuleName
	 * @return
	 */
	public boolean updateRuleRobotName(String ruleID, String newRuleName) {
		try {
			enterLock.writeLock().lock();
			for (int i = 0; i < workArea.size(); i++) {
				RuleRobot delegate = workArea.get(i);
				if (delegate.getRuleObject().getRuleID().equals(ruleID)) {
					delegate.getRuleObject().setName(newRuleName);
					return true;
				}
			}
			return false;

		} finally {
			enterLock.writeLock().unlock();
		}
	}

	/**
	 * 更新指定规则的enable状态
	 * 
	 * @param ruleID
	 * @param enabled
	 * @return
	 */
	public boolean updateRuleRobotEnableStatus(String ruleID, boolean enabled) {
		try {
			enterLock.writeLock().lock();
			for (int i = 0; i < workArea.size(); i++) {
				RuleRobot delegate = workArea.get(i);
				if (delegate.getRuleObject().getRuleID().equals(ruleID)) {
					delegate.getRuleObject().setEnabled(enabled);
					return true;
				}
			}
			return false;

		} finally {
			enterLock.writeLock().unlock();
		}
	}

	public void run() {
		LinkedBlockingQueue<SIMEventObject> queue=Processor.getInstance().getTransferData();
		while (true) {
			try {
				if (workArea == null || workArea.size() == 0) {
					continue;
				}
				SIMEventObject event = queue.take();
				try {
					enterLock.readLock().lock();
					for (int i = 0; i < workArea.size(); i++) {
						RuleRobot delegate = workArea.get(i);
						if (!delegate.getRuleObject().isEnabled())// 如果该规则没有启用，则不进行判断
							continue;
						try {
							boolean satisfied = delegate.isSatisfied(event);
							System.out.println(satisfied+"------------------------");
							if (satisfied) {
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} finally {
					enterLock.readLock().unlock();
				}
			} catch (Exception e) {
				logger.error("correlation thread error:", e);
			}

		}
	}

	

}

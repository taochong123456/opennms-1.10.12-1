package org.opennms.netmgt.syslogd.analyze;
import java.util.TimerTask;
public abstract class SchedulerTask implements Runnable {
	protected String name;// 此调度任务的名称，调度器Scheduler依据此名称定位调度任务，一个Scheduler中不允许加入同名的调度任务
	final Object lock = new Object();

	int state = VIRGIN;
	static final int VIRGIN = 0;
	static final int SCHEDULED = 1;
	static final int CANCELLED = 2;

	TimerTask timerTask;

	private Boolean active = Boolean.FALSE;
	private int interval = 3;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public TimerTask getTimerTask() {
		return timerTask;
	}

	public void setTimerTask(TimerTask timerTask) {
		this.timerTask = timerTask;
	}

	public String getName() {
		return name;
	}

	public Object getLock() {
		return lock;
	}

	public Boolean getActive() {
		return active;
	}

	public int getInterval() {
		return interval;
	}

	public static int getVIRGIN() {
		return VIRGIN;
	}

	public static int getSCHEDULED() {
		return SCHEDULED;
	}

	public static int getCANCELLED() {
		return CANCELLED;
	}

	/**
	 * 构造函数，创建一个调度任务
	 */
	public SchedulerTask(String name) {
		this.name = name;
	}

	/**
	 * 该调度任务被调度器调用时所要执行的方法
	 */
	public abstract void run();

	/**
	 * 撤销此调度任务
	 * 
	 * @return 返回执行结果，true执行成功，false执行失败
	 */
	public boolean cancel() {
		synchronized (lock) {
			if (timerTask != null) {
				timerTask.cancel();
			}
			boolean result = (state == SCHEDULED);
			state = CANCELLED;
			return result;
		}
	}

	/**
	 * 返回最近一次执行该调度任务的时间
	 * 
	 * @return 返回的下一次执行时间
	 */
	public long scheduledExecutionTime() {
		synchronized (lock) {
			return timerTask == null ? 0 : timerTask.scheduledExecutionTime();
		}
	}

	public synchronized Boolean isActive() {
		if (active == true) {
			interval--;
			if (interval <= 0) {
				active = false;
				interval = 3;
			}
		}
		return active;
	}

	public synchronized void setActive(Boolean isActive) {
		this.active = isActive;
		if (active == false)
			interval = 3;
	}
}

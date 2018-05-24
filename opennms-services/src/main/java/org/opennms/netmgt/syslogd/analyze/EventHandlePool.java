package org.opennms.netmgt.syslogd.analyze;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.opennms.netmgt.syslogd.parse.EventFormater;
import org.opennms.netmgt.syslogd.parse.EventParse;
import org.opennms.netmgt.syslogd.parse.SIMEventObject;

public class EventHandlePool {

	private HandleThread[] handlethreads = null;
	private int handlethreadsize = 10;
	private boolean stopHandle = false;
	public int HandleMaxNum = 100;
	public int handleindex = 0;
	private Map devMatchMap = Collections.synchronizedMap(new HashMap());

	/**
	 * 存储指定IP发送的syslog的原始字符编码信息，Map值对为形如
	 * <"10.70.1.1","gb2312">的式样，主要用于处理发送syslog的设备没有使用gbk编码发送syslog文本时的情况。 added
	 * by zhuzhen 20110702
	 */
	private final Properties syslogEncodingMap = new Properties();
	/**
	 * 字典表，用来在匹配设备的时候，将设备类型由字符对应到整型 added by liyue 2012-9-10
	 */
	private Map<String, Integer> devTypeMap = Collections
			.synchronizedMap(new HashMap());

	public EventHandlePool() {
		String num = System.getProperty("SIMEventHandleThread");
		if (num != null && !num.equals(""))
			handlethreadsize = Integer.parseInt(num);
		String maxnum = System.getProperty("SIMEventHandleThreadMaxNum");
		if (maxnum != null && !maxnum.equals(""))
			HandleMaxNum = Integer.parseInt(maxnum);
		handlethreads = new HandleThread[handlethreadsize];
	}

	/**
	 * 初始化
	 * 
	 * @return
	 */
	public boolean init() {
		initDevMatchMap();
		initSyslogEncodingMap();// added by zhuzhen 20110702
		for (int i = 0; i < handlethreads.length; i++) {
			handlethreads[i] = new HandleThread();
			handlethreads[i].setPriority(Thread.MAX_PRIORITY);
			if (!handlethreads[i].init())
				return false;
			handlethreads[i].start();
		}
		return true;
	}

	/**
	 * 初始化设备缓存
	 */
	private void initDevMatchMap() {
	}

	/**
	 * 初始化设备类型字典表映射集合 add by liyue 2012-9-10
	 */
	private void initDevTypeMap() {
	}

	/**
	 * 初始化syslog发送编码字符集 added by zhuzhen 20110702
	 */
	private void initSyslogEncodingMap() {
		syslogEncodingMap.clear();
		try {
			syslogEncodingMap.load(new FileReader(System
					.getProperty("manageserver.dir")
					+ "/ext/audit/event/syslogencoding.properties"));
		} catch (Exception e) {
		}
	}

	/**
	 * 释放资源
	 * 
	 */
	public void free() {
		for (int i = 0; i < handlethreads.length; i++) {
			handlethreads[i].setStop(true);
			handlethreads[i].free();
		}
	}

	/**
	 * 处理原始事件，为了解决阻塞问题，重复执行3次。
	 * 
	 * @param message
	 * @param host
	 */
	public void handleRawEvent(int type, String host, Object data) {
		if (stopHandle)
			return;
		for (int k = 0; k < HandleMaxNum; k++) {
			for (int i = handleindex; i < handlethreadsize; i++) {
				if (handlethreads[i].isSleep()) {
					handlethreads[i].setRawEvent(type, host, data);
					handleindex = i + 1;
					if (handleindex >= handlethreadsize)
						handleindex = 0;
					return;
				}
			}
			for (int i = 0; i < handleindex; i++) {
				if (handlethreads[i].isSleep()) {
					handlethreads[i].setRawEvent(type, host, data);
					handleindex = i + 1;
					if (handleindex >= handlethreadsize)
						handleindex = 0;
					return;
				}
			}

		}
	}

	/**
	 * 处理事件，为了解决阻塞问题，重复执行3次。
	 * 
	 * @param event
	 */
	public void handleEvent(SIMEventObject event) {
		if (stopHandle)
			return;
		for (int k = 0; k < HandleMaxNum; k++) {
			for (int i = handleindex; i < handlethreadsize; i++) {
				if (handlethreads[i].isSleep()) {
					handlethreads[i].setEvent(event);
					handleindex = i + 1;
					if (handleindex >= handlethreadsize)
						handleindex = 0;
					return;
				}
			}

			for (int i = 0; i < handleindex; i++) {
				if (handlethreads[i].isSleep()) {
					handlethreads[i].setEvent(event);
					handleindex = i + 1;
					if (handleindex >= handlethreadsize)
						handleindex = 0;
					return;
				}
			}

		}
	}

	/**
	 * 暂时停止处理，
	 * 
	 */
	private synchronized void stopHandle() {
		stopHandle = true;
	}

	/**
	 * 启动处理
	 * 
	 */
	private synchronized void startHandle() {
		stopHandle = false;
	}

	/**
	 * 更新处理线程池
	 * 
	 */
	public void updateHandlePool() {
		stopHandle();
		try {
			Thread.sleep(300);
		} catch (Exception e) {
		}
		free();
		init();
		startHandle();
	}

	/**
	 * 更新设备缓存
	 */
	public void updateDevMatchMap() {
		stopHandle();
		initDevMatchMap();
		startHandle();
	}

	/**
	 * 处理事件线程
	 * 
	 * @author wenhua
	 * 
	 */
	private class HandleThread extends Thread {
		private boolean isstop = false;
		private boolean issleep = false;

		private EventFormater eventFormater = null;
		private EventCache eventCache = EventCache.getInstance();
		private ArrayList<EventAction> eventActions = new ArrayList<EventAction>();
		private ArrayList<EventFilter> eventFilters = new ArrayList<EventFilter>();
		private EventParse syslogParse = null;
		private RuleEnginAction ruleAction = new RuleEnginAction();
		private SIMEventObject event = null;
		private int type;
		private String host;
		private Object data = null;
		private SIMEventParse simeventParse = null;

		/**
		 * 初始化处理环境
		 * 
		 * @return
		 */
		public boolean init() {
			try {
				// 创建事件处理类
				EventHandleFactory eventFactory = new DefaultEventHandleFactory();
				eventFormater = eventFactory.createEventFormater();
				ArrayList<EventAction> actions = eventFactory
						.createEventAction();
				for (int i = 0; i < actions.size(); i++) {
					EventAction action = actions.get(i);
					if (!action.init()) {
					} else {
						if (action instanceof EventFilter)
							eventFilters.add((EventFilter) action);
						else
							eventActions.add(action);
					}
				}

				String classname = System.getProperty("SIMEventParseClass");
				if (classname == null)
					classname = "secfox.soc.manage.audit.collect.SyslogParse";
				syslogParse = (EventParse) Class.forName(classname)
						.newInstance();
				classname = System.getProperty("FlowParseClass");
				if (classname == null)
					classname = "secfox.soc.manage.audit.collect.CustomFlowParse";

				simeventParse = SIMEventParse.getInstance();

				// 设置设备类型匹配
				eventFormater.initDeviceMap(devMatchMap);
				ruleAction.init();

			} catch (Exception e) {
				return false;
			}
			return true;
		}

		/**
		 * 释放资源
		 * 
		 */
		public void free() {
			try {
				setStop(true);
				for (int i = 0; i < eventActions.size(); i++) {
					eventActions.get(i).free();
				}
				devMatchMap.clear();
			} catch (Exception e) {
			}
		}

		/**
		 * 设置停止
		 * 
		 * @param isstop
		 */
		public void setStop(boolean isstop) {
			synchronized (this) {
				isstop = true;
				this.notify();
			}
		}

		/**
		 * 设置需要处理的原始事件
		 * 
		 * @param event
		 */
		public void setRawEvent(int type, String host, Object data) {
			synchronized (this) {
				this.type = type;
				this.host = host;
				this.data = data;
				this.issleep = false;
				this.notify();
			}
		}

		/**
		 * 设置需要处理的事件
		 * 
		 * @param event
		 */
		public void setEvent(SIMEventObject event) {
			synchronized (this) {
				this.event = event;
				this.issleep = false;
				this.notify();
			}
		}

		/**
		 * 判断线程是否空闲
		 * 
		 * @return boolean
		 */
		public boolean isSleep() {
			return issleep;
		}

		/**
		 * 执行过滤条件
		 * 
		 * @param event
		 * @return
		 */
		private boolean filterEvent(SIMEventObject event) {
			for (int i = 0; i < eventFilters.size(); i++) {
				if (eventFilters.get(i).handle(event) == EventFilter.EXCLUDE)
					return true;
			}
			return false;
		}

		/**
		 * 根据IP地址匹配设备，将设备类型等参数赋值到事件中。
		 * 
		 * @param event
		 */
		private void matchDevice(SIMEventObject event) {
		}

		/**
		 * 增加新设备，目前不使用。
		 * 
		 * @param event
		 */
		public void addDevice(SIMEventObject event) {
		}

		/**
		 * 处理事件
		 * 
		 * @param event
		 */
		private void handleSIMEvent(SIMEventObject event) {
			if (event == null)
				return;

			SIMEventObject[] newEvents = null;
			// 如果id=0，需要归一化
			if (event.ID == 0) {
				try {
					newEvents = eventFormater.format(event);
					matchDevice(event);
				} catch (Exception e) {
				}
			}
			// NetFlow事件
			else if (event.collectType == 6) {
				matchDevice(event);
			}

			if (newEvents != null) {
				for (int i = 0; i < newEvents.length; i++)
					actionEvent(newEvents[i]);
			} else
				actionEvent(event);
		}

		private void actionEvent(SIMEventObject event) {
			// 执行过滤条件
			if (filterEvent(event))
				return;

			// 赋予全局id
			event.receptTime = System.currentTimeMillis();

			// 处理Action
			ruleAction.handle(event);
			for (int i = 0; i < eventActions.size(); i++) {
				eventActions.get(i).handle(event);
			}

		}

		public void run() {
		}
	}
}

package org.opennms.netmgt.syslogd.parse;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import javax.xml.stream.EventFilter;
import org.opennms.netmgt.syslogd.analyze.DefaultEventHandleFactory;
import org.opennms.netmgt.syslogd.analyze.EventAction;
import org.opennms.netmgt.syslogd.analyze.EventHandleFactory;
import org.opennms.netmgt.syslogd.analyze.RuleEnginAction;

public class Processor extends Thread {
	private LinkedBlockingQueue<Data> queue = new LinkedBlockingQueue<Data>();
	private LinkedBlockingQueue<SIMEventObject> transferQueue = new LinkedBlockingQueue<SIMEventObject>();
	private ArrayList<EventAction> eventActions = new ArrayList<EventAction>();
	private ArrayList<EventFilter> eventFilters = new ArrayList<EventFilter>();
	private RuleEnginAction ruleAction = new RuleEnginAction();
	private static Processor instance = new Processor();

	public static Processor getInstance() {
		return instance;
	}

	public void setQueue(LinkedBlockingQueue<Data> queue) {
		this.queue = queue;
	}

	private Processor() {
	}

	public void init() {
		try {
			// 创建事件处理类
			EventHandleFactory eventFactory = new DefaultEventHandleFactory();
			ArrayList<EventAction> actions = eventFactory.createEventAction();
			for (int i = 0; i < actions.size(); i++) {
				EventAction action = actions.get(i);
				if (action instanceof EventFilter)
					eventFilters.add((EventFilter) action);
				else
					eventActions.add(action);
			}
			ruleAction.init();
		} catch (Exception e) {

		}
	}

	public void run() {
		try {
			EventFormater formater = new DefaultEventFormater();
			while (true) {
				Data data = queue.take();
				SIMEventObject event = SyslogParse.getInstance().parse(
						data.getData(), data.getSourceIp());
				SIMEventObject[] events = formater.format(event);
				if (events != null) {
					transferQueue.put(events[0]);
				}

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public LinkedBlockingQueue getTransferData() {
		return transferQueue;

	}

}

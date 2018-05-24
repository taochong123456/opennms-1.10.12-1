package org.opennms.netmgt.syslogd.analyze;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.opennms.netmgt.syslogd.parse.SIMEventObject;

public class AnalyzeQueueThread extends Thread {
	private static AnalyzeQueueThread queue = new AnalyzeQueueThread();
	private static BlockingQueue<SIMEventObject> eventQueue = new LinkedBlockingQueue<SIMEventObject>();
	public static AnalyzeQueueThread getInstance() {
		return queue;
	}
	public void put(SIMEventObject events) {
		try {
			eventQueue.put(events);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			SIMEventObject event = eventQueue.poll();
			System.out.println(event);
		}
	}

}

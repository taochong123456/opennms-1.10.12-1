package org.opennms.netmgt.syslogd.analyze;

import java.util.concurrent.LinkedBlockingQueue;

import org.opennms.netmgt.syslogd.parse.Data;
import org.opennms.netmgt.syslogd.parse.Processor;
import org.opennms.netmgt.syslogd.parse.UDPServer;

public class Bootstrap {
	public static void main(String[] args) {
		/*
		 * AnalyzeQueueThread.getInstance().start();
		 */LinkedBlockingQueue<Data> queue = new LinkedBlockingQueue<Data>();
		UDPServer server = new UDPServer(queue);
		Processor processor = Processor.getInstance();
		processor.setQueue(queue);
		server.start();
		processor.start();

		RuleEngineServiceImpl engine = new RuleEngineServiceImpl();
		engine.start();

	}
}

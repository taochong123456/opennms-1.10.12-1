package org.opennms.netmgt.syslogd.analyze;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;

import org.apache.log4j.Logger;
import org.opennms.netmgt.syslogd.parse.SIMEventObject;

public class EventTransmitAction implements EventAction {
	private Logger logger = Logger.getLogger(this.getClass().getName());
	final static int BUFFER_SIZE = 8 * 1024;
	private String name = null;
	private SIMEventParse parse = null;
	private DatagramSocket socket = null;
	private InetAddress dInetAddr = null;
	private int dInetPort = 8514;
	private Deflater compresser = new Deflater();
	private boolean isCrypto = false;
	private int proto = SIMEventParse.PROTO_SECFOX;
	private byte[] crptobuffer = new byte[BUFFER_SIZE];
	private EventFilter filter = null;
	private SendThread sender = null;
	private boolean isStop = false;

	private List<InetAddress> dInetAddrList = new ArrayList<InetAddress>();

	/**
	 * 获取Action名称
	 * 
	 * @return
	 */
	public String getName() {
		if (name == null)
			return "EventTransmitAction";
		return name;
	}

	/**
	 * 设置名称
	 * 
	 * @param name
	 * @return
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 释放资源
	 * 
	 * @return
	 */
	public void free() {
		isStop = true;

		if (parse != null)
			parse.free();
		if (socket != null)
			socket.close();
		socket = null;
	}

	// =======================================================================

	/**
	 * 初始化
	 * 
	 * @return
	 */
	public boolean init() {
		EventServiceConfig config = EventServiceConfig.getEventServiceConfig();
		String dAddrTmp = config.getConfig("SIMTransmitAddr");
		if (dAddrTmp == null)
			return false;
		else if (dAddrTmp.contains("127.0.0.1") || dAddrTmp.equals(""))
			return false;
		String[] dAddrs = dAddrTmp.split(",");
		try {
			for (int i = 0; i < dAddrs.length; i++) {
				String tmp = dAddrs[i];
				dInetAddrList.add(InetAddress.getByName(tmp));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		String dPort = config.getConfig("SIMTransmitPort");
		if (dPort != null)
			dInetPort = Integer.parseInt(dPort);
		String cryptoStr = config.getConfig("SIMTransmitCrypto");
		if (cryptoStr != null) {
			if (cryptoStr.toLowerCase().equals("true"))
				isCrypto = true;
			else
				isCrypto = false;
		} else
			isCrypto = false;

		String protoStr = config.getConfig("SIMTransmitProto");
		if (protoStr != null) {
			proto = Integer.parseInt(protoStr);
		}

		int cachenum = 6000;
		protoStr = System.getProperty("SIMEventActionCacheSize");
		if (protoStr != null) {
			cachenum = Integer.parseInt(protoStr);
		}

		isStop = false;
		try {
			parse = SIMEventParse.getInstance();

			String str = config.getConfig("SIMEventTransmitFilter");
			if (str != null && !str.equals("")) {
				filter = new EventFilterAction("SIMEventTransmitFilter");
				if (!filter.init())
					return false;
			}

			// dInetAddr = InetAddress.getByName(dAddrTmp);
			if (proto == SIMEventParse.PROTO_SYSLOG
					|| proto == SIMEventParse.PROTO_SECFOX) {
				socket = new DatagramSocket();
			} else {

				sender = new SendThread();
				sender.start();
			}
		} catch (Exception e) {
			logger.info("Init transmit action:", e);
		}
		return true;
	}

	/*
	 * 发送事件线程
	 */
	private class SendThread extends Thread {
		public void run() {
		}
	}

	/**
	 * 处理事件
	 * 
	 * @param event
	 */
	public int handle(SIMEventObject event) {
		if (filter != null) {
			if (filter.handle(event) == EventFilter.EXCLUDE)
				return 1;
		}

		byte[] data = null;
		if (proto == SIMEventParse.PROTO_SYSLOG) {
			String str = parse.writeSIMEventStr(event);
			if (str == null)
				return 1;
			data = str.getBytes();
		} else if (proto == SIMEventParse.PROTO_SECFOX) {
			data = parse.writeSIMEvent(event);
			if (data == null)
				return 1;
		} else {
		}

		if (isCrypto) {
			compresser.reset();
			compresser.setInput(data);
			compresser.finish();
			int length = compresser.deflate(crptobuffer);
			try {
				if (socket != null) {
					if (null != dInetAddrList && dInetAddrList.size() > 0) {
						for (int i = 0; i < dInetAddrList.size(); i++) {
							dInetAddr = dInetAddrList.get(i);
							DatagramPacket dp = new DatagramPacket(crptobuffer,
									0, length, dInetAddr, dInetPort);
							socket.send(dp);
						}
					}
				}
			} catch (IOException e) {
				logger.info("Transmit event:", e);
				return 0;
			}
		} else {
			try {
				if (socket != null) {
					if (null != dInetAddrList && dInetAddrList.size() > 0) {
						for (int i = 0; i < dInetAddrList.size(); i++) {
							dInetAddr = dInetAddrList.get(i);
							DatagramPacket dp = new DatagramPacket(data, 0,
									data.length, dInetAddr, dInetPort);
							socket.send(dp);
						}
					}
				}
			} catch (IOException e) {
				logger.info("Transmit event:", e);
				return 0;
			}
		}
		return 1;
	}

	public static void main(String[] args) {
		SIMEventObject logOb = new SIMEventObject();
		logOb.ID = 0;
		// logOb.oriPriority = String.valueOf(1);
		logOb.receptTime = System.currentTimeMillis();

		// logOb.sendTime = logOb.occurTime;
		logOb.msg = "test message";
		// logOb.program = "processName";
		// logOb.devAddr = "10.70.23.99";
		logOb.sysType = 3;
		logOb.collectType = 1;
		// logOb.collectorAddr = "127.0.0.1";
		logOb.collectorName = "127.0.0.1";
		;
		EventTransmitAction action = new EventTransmitAction();
		EventServiceConfig config = EventServiceConfig.getEventServiceConfig();
		// config.setConfig("SIMTransmitAddr", "10.70.25.189");
		// config.setConfig("SIMTransmitAddr", "10.70.5.18");
		config.setConfig("SIMTransmitAddr", "10.70.25.189,10.70.5.18");
		config.setConfig("SIMTransmitPort", "9514");

		/**
		 * 转发单个地址
		 */
		action.init();
		action.handle(logOb);
		/**
		 * 转发多个地址
		 */
		// action.init_My();
		// action.handle_My(logOb);
	}
}

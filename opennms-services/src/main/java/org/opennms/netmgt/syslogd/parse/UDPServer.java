package org.opennms.netmgt.syslogd.parse;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.LinkedBlockingQueue;

public class UDPServer extends Thread {
	public int UDP_PORT = 514;
	public int UDP_MAX_DATA = 64 * 1024;
	public int receiveBufferSize = 1024 * 1024 * 300;// 300M����
	private DatagramSocket udpServerSocket = null;
	private boolean running = false;
	long packetCount = 0;//
	private LinkedBlockingQueue<Data> queue = new LinkedBlockingQueue<Data>();

	public UDPServer(LinkedBlockingQueue<Data> queue) {
		this.queue = queue;
	}

	public boolean isRunning() {
		return running;
	}

	public void run() {
		try {
			udpServerSocket = new DatagramSocket(UDP_PORT);
			udpServerSocket.setReceiveBufferSize(receiveBufferSize);
			byte[] buf = new byte[UDP_MAX_DATA];
			DatagramPacket myDataPacket = new DatagramPacket(buf, buf.length);
			running = true;
			while (running && !isInterrupted()) {
				udpServerSocket.receive(myDataPacket);
				Data data = new Data();
				String source = myDataPacket.getAddress().getHostAddress();
				String content = new String(myDataPacket.getData(), "gb2312");
				data.setData(content);
				data.setSourceIp(source);
				queue.put(data);
				packetCount++;
			}
		} catch (java.net.BindException e) {
		} catch (Exception e) {
		} finally {
		}

	}

	public void shutdown() {
		running = false;
		packetCount = 0;
		if (udpServerSocket != null && !udpServerSocket.isClosed()) {
			udpServerSocket.close();
		}
		interrupt();
	}

	public boolean isActive() {
		if (running) {
			if (udpServerSocket != null && udpServerSocket.isBound())
				return true;
		}
		return false;
	}
}
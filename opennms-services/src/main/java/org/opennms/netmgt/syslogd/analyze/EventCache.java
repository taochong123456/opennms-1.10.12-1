package org.opennms.netmgt.syslogd.analyze;

import java.io.*;
import java.net.DatagramPacket;

import org.apache.log4j.Logger;

public class EventCache {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private static long simeventid = 0;
	private static EventCache cache = null;
	private static String datafile = System.getProperty("manageserver.dir")
			+ "/data/eventcache.data.";
	private static String eventidfile = System.getProperty("manageserver.dir")
			+ "/data/simevent.id";

	private DataOutputStream dataCacheWriter = null;
	private DataInputStream dataCacheReader = null;
	private int writeindex = 0;
	private int readindex = 0;
	private long writepoint = 0;
	private long readpoint = 0;
	private long cachepoint = 0;
	private long eventNum = 0;

	private long EVENTSIZE = 100000;
	private int MAX_INDEX = 30;
	private long MAXEVENT = EVENTSIZE * (MAX_INDEX - 2);

	/**
	 * 初始化参数，从缓存的文件建立文件定位。
	 */
	public boolean init() {
		return false;
	}

	/**
	 * 获得缓存实例。
	 * 
	 * @return
	 */
	public static EventCache getInstance() {
		if (cache == null)
			cache = new EventCache();
		return cache;
	}

	/**
	 * 获取事件全局ID。
	 * 
	 * @return
	 */
	public synchronized long getEventID() {
		return 0;
	}

	/**
	 * 从缓存中读取事件
	 * 
	 * @return
	 */
	public synchronized Object[] getEvent() {
		if (readindex == writeindex && readpoint == writepoint) {
			return null;
		}

		Object[] eventob = new Object[3];
		try {
			if (dataCacheReader == null) {
				dataCacheReader = new DataInputStream(new BufferedInputStream(
						new FileInputStream(datafile + readindex)));
			}
			eventob[0] = dataCacheReader.readInt();
			eventob[1] = dataCacheReader.readUTF();
			int size = dataCacheReader.readInt();
			byte[] data = new byte[size];
			dataCacheReader.read(data, 0, size);
			eventob[2] = data;
			// 读换行符'\n'
			dataCacheReader.read();
			readpoint++;
		} catch (Exception e) {
			try {
				if (readindex != writeindex) {
					dataCacheReader.close();
					if (readindex >= MAX_INDEX)
						readindex = 0;
					else
						readindex++;
					dataCacheReader = new DataInputStream(
							new BufferedInputStream(new FileInputStream(
									datafile + readindex)));
					eventob[0] = dataCacheReader.readInt();
					eventob[1] = dataCacheReader.readUTF();
					int size = dataCacheReader.readInt();
					byte[] data = new byte[size];
					dataCacheReader.read(data, 0, size);
					eventob[2] = data;
					// 读换行符'\n'
					dataCacheReader.read();
					readpoint = 1;
				}
			} catch (Exception ex) {
				logger.info("Read event cache:" + e.getMessage());
				eventob = null;
			}
		}
		cachepoint--;
		return eventob;
	}

	/**
	 * 设置新加的事件。
	 * 
	 * @param event
	 * @throws IOException
	 */
	public synchronized void putEvent(int type, String host, byte[] data,
			int size) throws IOException {
		eventNum++;
		if (cachepoint > MAXEVENT)
			return;

		if (writepoint > EVENTSIZE) {
			if (dataCacheWriter != null)
				dataCacheWriter.close();
			if (writeindex >= MAX_INDEX)
				writeindex = 0;
			else
				writeindex++;

			dataCacheWriter = new DataOutputStream(new BufferedOutputStream(
					new FileOutputStream(datafile + writeindex)));
			writepoint = 0;
		}

		dataCacheWriter.writeInt(type);
		dataCacheWriter.writeUTF(host);
		dataCacheWriter.writeInt(size);
		dataCacheWriter.write(data, 0, size);
		dataCacheWriter.write((int) '\n');
		dataCacheWriter.flush();
		writepoint++;
		cachepoint++;
	}

	/**
	 * 设置每个缓存文件存储的事件个数，一般可采用默认100000。
	 */
	public void setCacheSize(long num) {
		long size = num / MAX_INDEX;
		if (size > 1000) {
			EVENTSIZE = size;
			MAXEVENT = EVENTSIZE * (MAX_INDEX - 2);
		}
	}

	/**
	 * 释放文件资源
	 * 
	 */
	public void free() {
	}

	/**
	 * 读取当前的日志数目
	 * 
	 * @return
	 */
	public long getEventNum() {
		return eventNum;
	}

}

package org.opennms.netmgt.syslogd.analyze;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.opennms.netmgt.syslogd.parse.SIMEventObject;

public class SIMEventParse {
	public static int EVENT_MAX_DATA = 8 * 1024;
	public static int PROTO_SYSLOG = 2;
	public static int PROTO_SECFOX = 1;

	private byte[] eventData = new byte[EVENT_MAX_DATA];
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private DataOutputStream dataOutput = null;
	private DataInputStream dataInput = null;
	private ByteArrayOutputStream byteOutput = null;
	private ByteArrayInputStream byteInput = null;
	private Pattern eventPattern = null;

	/**
	 * 创建解析类
	 * 
	 * @return
	 */
	public static SIMEventParse getInstance() {
		return new SIMEventParse();
	}

	public SIMEventParse() {
		String regex = "ID=([^receptTime=]+)receptTime=([^aggregatedCount=]+)aggregatedCount=([^sysType=]+)sysType=([^collectType=]+)collectType=([^sensorAddr=]+)sensorAddr=([^sensorName=]+)sensorName=([^collectorAddr=]+)collectorAddr=([^collectorName=]+)collectorName=([^category=]+)category=([^type=]+)type=([^priority=]+)priority=([^oriPriority=]+)oriPriority=([^rawID=]+)rawID=([^occurTime=]+)occurTime=([^sendTime=]+)sendTime=([^duration=]+)duration=([^send=]+)send=([^receive=]+)receive=([^protocol=]+)protocol=([^appProtocol=]+)appProtocol=([^object=]+)object=([^policy=]+)policy=([^resource=]+)resource=([^action=]+)action=([^intent=]+)intent=([^result=]+)result=([^sAddr=]+)sAddr=([^sName=]+)sName=([^sPort=]+)sPort=([^sProcess=]+)sProcess=([^sUserID=]+)sUserID=([^sUserName=]+)sUserName=([^stAddr=]+)stAddr=([^stPort=]+)stPort=([^dAddr=]+)dAddr=([^dName=]+)dName=([^dPort=]+)dPort=([^dProcess=]+)dProcess=([^dUserID=]+)dUserID=([^dUserName=]+)dUserName=([^dtAddr=]+)dtAddr=([^dtPort=]+)dtPort=([^devAddr=]+)devAddr=([^devName=]+)devName=([^devCategory=]+)devCategory=([^devType=]+)devType=([^devVendor=]+)devVendor=([^programType=]+)programType=([^program=]+)program=([^requestURI=]+)requestURI=([^name=]+)name=([^customS1=]+)customS1=([^customS2=]+)customS2=([^customS3=]+)customS3=([^customS4=]+)customS4=([^customD1=]+)customD1=([^customD2=]+)customD2=([^devProduct=]+)devProduct=([^sessionID=]+)sessionID=([^customS5=]+)customS5=([^customS6=]+)customS6=([^customS7=]+)customS7=([^customD3=]+)customD3=([^customD4=]+)customD4=([^msg=]+)msg=(.*)";
		try {
			eventPattern = Pattern.compile(regex);
		} catch (Exception e) {
			logger.info("Create eventPattern: ", e);
		}

		byteInput = new ByteArrayInputStream(eventData);
		dataInput = new DataInputStream(byteInput);
		byteOutput = new ByteArrayOutputStream(EVENT_MAX_DATA);
		dataOutput = new DataOutputStream(byteOutput);
	}

	public void free() {
		try {
			if (dataOutput != null)
				dataOutput.close();
			if (dataInput != null)
				dataInput.close();
		} catch (IOException e) {
			logger.info("Free simevent parse: ", e);
		}
	}

	/**
	 * 将事件写入到字符串中 针对新版事件属性字段，地址类字段在传输中都是字符形式，即“10.1.1.1”与“fe80::2”形式，
	 * 只有在涉及存储数据库的时候才转换为byte[ ]形式
	 * 
	 * @return
	 */
	public String writeSIMEventStr(SIMEventObject event) {
		StringBuffer strB = new StringBuffer();
		strB.append("ID=");
		strB.append(event.getID());
		strB.append(" receptTime=");
		strB.append(event.getReceptTime());
		strB.append(" aggregatedCount=");
		strB.append(event.getAggregatedCount());
		strB.append(" sysType=");
		strB.append(event.getSysType());
		strB.append(" collectType=");
		strB.append(event.getCollectType());
		// strB.append(" sensorAddr=");
		// strB.append(event.getSensorAddr()); //新版本事件属性已取消字段
		// strB.append(" sensorName=");
		// strB.append(event.getSensorName());
		strB.append(" collectorAddr=");
		strB.append(event.getCollectorAddr());
		strB.append(" collectorName=");
		strB.append(event.getCollectorName());
		strB.append(" category=");
		strB.append(event.getCategory());
		strB.append(" type=");
		strB.append(event.getType());
		strB.append(" priority=");
		if (event.getPriority() != null)
			strB.append(event.getPriority());
		// strB.append(" oriPriority=");
		// strB.append(event.getOriPriority()); //新版本事件属性已取消字段
		strB.append(" rawID=");
		if (event.getRawID() != null)
			strB.append(event.getRawID());
		strB.append(" occurTime=");
		if (event.getOccurTime() != null)
			strB.append(event.getOccurTime());
		// strB.append(" sendTime=");
		// if (event.getSendTime()!=null)
		// strB.append(event.getSendTime()); //新版本事件属性已取消字段
		strB.append(" duration=");
		if (event.getDuration() != null)
			strB.append(event.getDuration());
		strB.append(" send=");
		if (event.getSend() != null)
			strB.append(event.getSend());
		strB.append(" receive=");
		if (event.getReceive() != null)
			strB.append(event.getReceive());
		strB.append(" protocol=");
		if (event.getProtocol() != null)
			strB.append(event.getProtocol());
		strB.append(" appProtocol=");
		if (event.getAppProtocol() != null)
			strB.append(event.getAppProtocol());
		strB.append(" object=");
		strB.append(event.getObject());
		strB.append(" policy=");
		strB.append(event.getPolicy());
		strB.append(" resource=");
		strB.append(event.getResource());
		strB.append(" action=");
		strB.append(event.getAction());
		strB.append(" intent=");
		strB.append(event.getIntent());
		strB.append(" result=");
		strB.append(event.getResult());
		strB.append(" sAddr=");
		strB.append(event.getSAddr());
		// strB.append(" sName=");
		// strB.append(event.getSName()); //新版本事件属性已取消字段
		strB.append(" sPort=");
		if (event.getSPort() != null)
			strB.append(event.getSPort());
		// strB.append(" sProcess=");
		// strB.append(event.getSProcess()); //新版本事件属性已取消字段
		// strB.append(" sUserID=");
		// strB.append(event.getSUserID()); //新版本事件属性已取消字段
		strB.append(" sUserName=");
		strB.append(event.getSUserName());
		strB.append(" stAddr=");
		strB.append(event.getStAddr());
		strB.append(" stPort=");
		if (event.getStPort() != null)
			strB.append(event.getStPort());
		strB.append(" dAddr=");
		strB.append(event.getDAddr());
		// strB.append(" dName=");
		// strB.append(event.getDName()); //新版本事件属性已取消字段
		strB.append(" dPort=");
		if (event.getDPort() != null)
			strB.append(event.getDPort());
		// strB.append(" dProcess=");
		// strB.append(event.getDProcess()); //新版本事件属性已取消字段
		// strB.append(" dUserID=");
		// strB.append(event.getDUserID()); //新版本事件属性已取消字段
		strB.append(" dUserName=");
		strB.append(event.getDUserName());
		strB.append(" dtAddr=");
		strB.append(event.getDtAddr());
		strB.append(" dtPort=");
		if (event.getDtPort() != null)
			strB.append(event.getDtPort());
		strB.append(" devAddr=");
		strB.append(event.getDevAddr());
		strB.append(" devName=");
		strB.append(event.getDevName());
		strB.append(" devCategory=");
		if (event.getDevCategory() != null)
			strB.append(event.getDevCategory());
		strB.append(" devType=");
		strB.append(event.getDevType());
		strB.append(" devVendor=");
		strB.append(event.getDevVendor());
		strB.append(" programType=");
		strB.append(event.getProgramType());
		// strB.append(" program=");
		// strB.append(event.getProgram()); //新版本事件属性已取消字段
		strB.append(" requestURI=");
		strB.append(event.getRequestURI());
		strB.append(" name=");
		strB.append(event.getName());
		strB.append(" customS1=");
		strB.append(event.getCustomS1());
		strB.append(" customS2=");
		strB.append(event.getCustomS2());
		strB.append(" customS3=");
		strB.append(event.getCustomS3());
		strB.append(" customS4=");
		strB.append(event.getCustomS4());
		strB.append(" customD1=");
		if (event.getCustomD1() != null)
			strB.append(event.getCustomD1());
		strB.append(" customD2=");
		if (event.getCustomD2() != null)
			strB.append(event.getCustomD2());
		strB.append(" devProduct=");
		strB.append(event.getDevProduct());
		strB.append(" sessionID=");
		strB.append(event.getSessionID());
		// strB.append(" customS5=");
		// strB.append(event.getCustomS5()); //新版本事件属性已取消字段
		// strB.append(" customS6=");
		// strB.append(event.getCustomS6()); //新版本事件属性改为sMAC字段
		strB.append(" sMAC=");
		strB.append(event.getSMAC());
		// strB.append(" customS7=");
		// strB.append(event.getCustomS7()); //新版本事件属性改为dMAC字段
		strB.append(" dMAC=");
		strB.append(event.getDMAC());
		strB.append(" customD3=");
		if (event.getCustomD3() != null)
			strB.append(event.getCustomD3());
		strB.append(" customD4=");
		if (event.getCustomD4() != null)
			strB.append(event.getCustomD4());
		strB.append(" msg=");
		strB.append(event.getMsg());
		return strB.toString();
	}

	/**
	 * 将事件写入到数据buffer中。
	 * 
	 * @param event
	 */
	public byte[] writeSIMAlert(SIMAlertObject event) {
		return null;
	}

	/**
	 * 将事件写入到数据buffer中。
	 * 
	 * @param event
	 */
	public byte[] writeSIMEvent(SIMEventObject event) {
		try {
			byteOutput.reset();
			dataOutput.writeLong(event.ID);
			dataOutput.writeLong(event.receptTime);
			dataOutput.writeLong(event.aggregatedCount);
			dataOutput.writeInt(event.sysType);
			dataOutput.writeInt(event.collectType);
			// dataOutput.writeUTF(event.sensorAddr); //新版本事件属性已取消字段
			// dataOutput.writeUTF(event.sensorName); //新版本事件属性已取消字段
			dataOutput.writeUTF(event.collectorAddr);
			dataOutput.writeUTF(event.collectorName);
			// dataOutput.writeUTF(event.category); //新版本事件属性已改变类型字段
			if (event.category == null)
				dataOutput.writeInt(-1);
			else
				dataOutput.writeInt(event.category);
			// dataOutput.writeUTF(event.type); //新版本事件属性已改变类型字段
			if (event.type == null)
				dataOutput.writeInt(-1);
			else
				dataOutput.writeInt(event.type);
			if (event.priority == null)
				dataOutput.writeInt(-1);
			else
				dataOutput.writeInt(event.priority);
			// dataOutput.writeUTF(event.oriPriority); //新版本事件属性已取消字段
			if (event.rawID == null)
				dataOutput.writeLong(-1);
			else
				dataOutput.writeLong(event.rawID);
			if (event.occurTime == null)
				dataOutput.writeLong(-1);
			else
				dataOutput.writeLong(event.occurTime);
			// if (event.sendTime==null)
			// dataOutput.writeLong(-1);
			// else
			// dataOutput.writeLong(event.sendTime); //新版本事件属性已取消字段
			if (event.duration == null)
				dataOutput.writeLong(-1);
			else
				dataOutput.writeLong(event.duration);
			if (event.send == null)
				dataOutput.writeLong(-1);
			else
				dataOutput.writeLong(event.send);
			if (event.receive == null)
				dataOutput.writeLong(-1);
			else
				dataOutput.writeLong(event.receive);
			if (event.protocol == null)
				dataOutput.writeInt(-1);
			else
				dataOutput.writeInt(event.protocol);
			if (event.appProtocol == null)
				dataOutput.writeInt(-1);
			else
				dataOutput.writeInt(event.appProtocol);
			// dataOutput.writeUTF(event.object); //新版本事件属性已改变类型字段
			if (event.object == null)
				dataOutput.writeInt(-1);
			else
				dataOutput.writeInt(event.object);
			// dataOutput.writeUTF(event.policy); //新版本事件属性已改变类型字段
			if (event.policy == null)
				dataOutput.writeInt(-1);
			else
				dataOutput.writeInt(event.policy);
			dataOutput.writeUTF(event.resource);
			// dataOutput.writeUTF(event.action); //新版本事件属性已改变类型字段
			if (event.action == null)
				dataOutput.writeInt(-1);
			else
				dataOutput.writeInt(event.action);
			dataOutput.writeUTF(event.intent);
			// dataOutput.writeUTF(event.result); //新版本事件属性已改变类型字段
			if (event.result == null)
				dataOutput.writeInt(-1);
			else
				dataOutput.writeInt(event.result);
			dataOutput.writeUTF(event.sAddr);
			// dataOutput.writeUTF(event.sName); //新版本事件属性已取消字段
			if (event.sPort == null)
				dataOutput.writeInt(-1);
			else
				dataOutput.writeInt(event.sPort);
			// dataOutput.writeUTF(event.sProcess); //新版本事件属性已取消字段
			// dataOutput.writeUTF(event.sUserID); //新版本事件属性已取消字段
			dataOutput.writeUTF(event.sUserName);
			dataOutput.writeUTF(event.stAddr);
			if (event.stPort == null)
				dataOutput.writeInt(-1);
			else
				dataOutput.writeInt(event.stPort);
			dataOutput.writeUTF(event.dAddr);
			// dataOutput.writeUTF(event.dName); //新版本事件属性已取消字段
			if (event.dPort == null)
				dataOutput.writeInt(-1);
			else
				dataOutput.writeInt(event.dPort);
			// dataOutput.writeUTF(event.dProcess); //新版本事件属性已取消字段
			// dataOutput.writeUTF(event.dUserID); //新版本事件属性已取消字段
			dataOutput.writeUTF(event.dUserName);
			dataOutput.writeUTF(event.dtAddr);
			if (event.dtPort == null)
				dataOutput.writeInt(-1);
			else
				dataOutput.writeInt(event.dtPort);
			dataOutput.writeUTF(event.devAddr);
			dataOutput.writeUTF(event.devName);
			if (event.devCategory == null)
				dataOutput.writeInt(-1);
			else
				dataOutput.writeInt(event.devCategory);
			// dataOutput.writeUTF(event.devType); //新版本事件属性已改变类型字段
			if (event.devType == null)
				dataOutput.writeInt(-1);
			else
				dataOutput.writeInt(event.devType);
			dataOutput.writeUTF(event.devVendor);
			dataOutput.writeUTF(event.programType);
			// dataOutput.writeUTF(event.program); //新版本事件属性已取消字段
			dataOutput.writeUTF(event.requestURI);
			dataOutput.writeUTF(event.name);
			dataOutput.writeUTF(event.customS1);
			dataOutput.writeUTF(event.customS2);
			dataOutput.writeUTF(event.customS3);
			dataOutput.writeUTF(event.customS4);
			if (event.customD1 == null)
				dataOutput.writeLong(-1);
			else
				dataOutput.writeLong(event.customD1);
			if (event.customD2 == null)
				dataOutput.writeLong(-1);
			else
				dataOutput.writeLong(event.customD2);
			dataOutput.writeUTF(event.devProduct);
			dataOutput.writeUTF(event.sessionID);
			// dataOutput.writeUTF(event.customS5); //新版本事件属性已取消字段
			// dataOutput.writeUTF(event.customS6); //新版本事件属性已改变为sMAC
			dataOutput.writeUTF(event.sMAC);
			// dataOutput.writeUTF(event.customS7); //新版本事件属性已改变为dMAC
			dataOutput.writeUTF(event.dMAC);
			if (event.customD3 == null)
				dataOutput.writeDouble(-1);
			else
				dataOutput.writeDouble(event.customD3);
			if (event.customD4 == null)
				dataOutput.writeDouble(-1);
			else
				dataOutput.writeDouble(event.customD4);
			dataOutput.writeUTF(event.msg);
			dataOutput.flush();
		} catch (IOException e) {
			logger.info("Write simevent: " + e.getMessage());
			return null;
		}
		return byteOutput.toByteArray();
	}

	/**
	 * 从字符串中读取事件 读取字段的顺序与writeSIMEventStr一致 新版本事件属性字段的顺序做了修改
	 * 
	 * @param data
	 * @return
	 */
	public SIMEventObject readSIMEventStr(String data) {
		SIMEventObject event = new SIMEventObject();
		try {
			Matcher m = eventPattern.matcher(data);
			if (!m.find())
				return null;

			event.ID = Long.parseLong(m.group(1).trim());
			event.receptTime = Long.parseLong(m.group(2).trim());
			event.aggregatedCount = Long.parseLong(m.group(3).trim());
			event.sysType = Integer.parseInt(m.group(4).trim());
			event.collectType = Integer.parseInt(m.group(5).trim());
			// event.sensorAddr=m.group(6).trim(); //新版本事件属性已取消字段
			// event.sensorName=m.group(7).trim(); //新版本事件属性已取消字段
			event.collectorAddr = m.group(6).trim();
			event.collectorName = m.group(7).trim();
			// event.category=m.group(10).trim(); //新版本事件属性已改变类型字段
			if (!m.group(8).equals(" "))
				event.category = Integer.parseInt(m.group(8).trim());
			// event.type=m.group(11).trim(); //新版本事件属性已改变类型字段
			if (!m.group(9).equals(" "))
				event.type = Integer.parseInt(m.group(9).trim());
			if (!m.group(10).equals(" "))
				event.priority = Integer.parseInt(m.group(10).trim());
			// event.oriPriority=m.group(13).trim(); //新版本事件属性已取消字段
			if (!m.group(11).equals(" "))
				event.rawID = Long.parseLong(m.group(11).trim());
			if (!m.group(12).equals(" "))
				event.occurTime = Long.parseLong(m.group(12).trim());
			// if (!m.group(16).equals(" "))
			// event.sendTime=Long.parseLong(m.group(16).trim()); //新版本事件属性已取消字段
			if (!m.group(13).equals(" "))
				event.duration = Long.parseLong(m.group(13).trim());
			if (!m.group(14).equals(" "))
				event.send = Long.parseLong(m.group(14).trim());
			if (!m.group(15).equals(" "))
				event.receive = Long.parseLong(m.group(15).trim());
			if (!m.group(16).equals(" "))
				event.protocol = Integer.parseInt(m.group(16).trim());
			if (!m.group(17).equals(" "))
				event.appProtocol = Integer.parseInt(m.group(17).trim());
			// event.object=m.group(22).trim(); //新版本事件属性已改变类型字段
			if (!m.group(18).equals(" "))
				event.object = Integer.parseInt(m.group(18).trim());
			// event.policy=m.group(23).trim(); //新版本事件属性已改变类型字段
			if (!m.group(19).equals(" "))
				event.policy = Integer.parseInt(m.group(19).trim());
			event.resource = m.group(20).trim();
			// event.action=m.group(25).trim(); //新版本事件属性已改变类型字段
			if (!m.group(21).equals(" "))
				event.action = Integer.parseInt(m.group(21).trim());
			event.intent = m.group(22).trim();
			// event.result=m.group(27).trim(); //新版本事件属性已改变类型字段
			if (!m.group(23).equals(" "))
				event.result = Integer.parseInt(m.group(23).trim());
			event.sAddr = m.group(24).trim();
			// event.sName=m.group(39).trim(); //新版本事件属性已取消字段
			if (!m.group(25).equals(" "))
				event.sPort = Integer.parseInt(m.group(25).trim());
			// event.sProcess=m.group(31).trim(); //新版本事件属性已取消字段
			// event.sUserID=m.group(32).trim(); //新版本事件属性已取消字段
			event.sUserName = m.group(26).trim();
			event.stAddr = m.group(27).trim();
			if (!m.group(28).equals(" "))
				event.stPort = Integer.parseInt(m.group(28).trim());
			event.dAddr = m.group(29).trim();
			// event.dName=m.group(37).trim(); //新版本事件属性已取消字段
			if (!m.group(30).equals(" "))
				event.dPort = Integer.parseInt(m.group(30).trim());
			// event.dProcess=m.group(39).trim(); //新版本事件属性已取消字段
			// event.dUserID=m.group(40).trim(); //新版本事件属性已取消字段
			event.dUserName = m.group(31).trim();
			event.dtAddr = m.group(32).trim();
			if (!m.group(33).equals(" "))
				event.dtPort = Integer.parseInt(m.group(33).trim());
			event.devAddr = m.group(34).trim();
			event.devName = m.group(35).trim();
			if (!m.group(36).equals(" "))
				event.devCategory = Integer.parseInt(m.group(36).trim());
			// event.devType=m.group(47).trim(); //新版本事件属性已改变类型字段
			if (!m.group(37).equals(" "))
				event.devType = Integer.parseInt(m.group(37).trim());
			event.devVendor = m.group(38).trim();
			event.programType = m.group(39).trim();
			// event.program=m.group(50).trim(); //新版本事件属性已取消字段
			event.requestURI = m.group(40).trim();
			event.name = m.group(41).trim();
			event.customS1 = m.group(42).trim();
			event.customS2 = m.group(43).trim();
			event.customS3 = m.group(44).trim();
			event.customS4 = m.group(45).trim();
			if (!m.group(46).equals(" "))
				event.customD1 = Long.parseLong(m.group(46).trim());
			if (!m.group(47).equals(" "))
				event.customD2 = Long.parseLong(m.group(47).trim());
			event.devProduct = m.group(48).trim();
			event.sessionID = m.group(49).trim();
			// event.customS5=m.group(61).trim(); //新版本事件属性已取消字段
			// event.customS6=m.group(62).trim(); //新版本事件属性已改变为sMAC
			event.sMAC = m.group(50).trim();
			// event.customS7=m.group(63).trim(); //新版本事件属性已改变为dMAC
			event.dMAC = m.group(51).trim();
			if (!m.group(52).equals(" "))
				event.customD3 = Double.parseDouble(m.group(52).trim());
			if (!m.group(53).equals(" "))
				event.customD4 = Double.parseDouble(m.group(53).trim());
			// event.msg=m.group(66); //新版本事件属性已取消字段
		} catch (Exception e) {
			logger.info("Read simevent: " + e.getMessage());
			return null;
		}
		return event;
	}

	/**
	 * 从数据buffer中读取事件。
	 * 
	 * @return SIMEventObject
	 */
	public SIMEventObject readSIMEvent(byte[] data, int length) {
		System.arraycopy(data, 0, eventData, 0, length);
		SIMEventObject event = new SIMEventObject();
		try {
			byteInput.reset();
			dataInput.reset();
			event.ID = dataInput.readLong();
			event.receptTime = dataInput.readLong();
			event.aggregatedCount = dataInput.readLong();
			event.sysType = dataInput.readInt();
			event.collectType = dataInput.readInt();
			// event.sensorAddr = dataInput.readUTF(); //新版本事件属性已取消字段
			// event.sensorName = dataInput.readUTF(); //新版本事件属性已取消字段
			event.collectorAddr = dataInput.readUTF();
			event.collectorName = dataInput.readUTF();
			// event.category = dataInput.readUTF(); //新版本事件属性已改变类型字段
			event.category = dataInput.readInt();
			if (event.category == -1)
				event.category = null;
			// event.type = dataInput.readUTF(); //新版本事件属性已改变类型字段
			event.type = dataInput.readInt();
			if (event.type == -1)
				event.type = null;
			event.priority = dataInput.readInt();
			if (event.priority == -1)
				event.priority = null;
			// event.oriPriority = dataInput.readUTF(); //新版本事件属性已取消字段
			event.rawID = dataInput.readLong();
			if (event.rawID == -1)
				event.rawID = null;
			event.occurTime = dataInput.readLong();
			if (event.occurTime == -1)
				event.occurTime = null;
			// event.sendTime = dataInput.readLong(); //新版本事件属性已取消字段
			// if (event.sendTime==-1)
			// event.sendTime=null;
			event.duration = dataInput.readLong();
			if (event.duration == -1)
				event.duration = null;
			event.send = dataInput.readLong();
			if (event.send == -1)
				event.send = null;
			event.receive = dataInput.readLong();
			if (event.receive == -1)
				event.receive = null;
			event.protocol = dataInput.readInt();
			if (event.protocol == -1)
				event.protocol = null;
			event.appProtocol = dataInput.readInt();
			if (event.appProtocol == -1)
				event.appProtocol = null;
			// event.object = dataInput.readUTF();//<-----------对象
			// //新版本事件属性已改变类型字段
			event.object = dataInput.readInt();
			if (event.object == -1)
				event.object = null;
			// event.policy = dataInput.readUTF(); //新版本事件属性已改变类型字段
			event.policy = dataInput.readInt();
			if (event.policy == -1)
				event.policy = null;
			event.resource = dataInput.readUTF();
			// event.action = dataInput.readUTF(); //新版本事件属性已改变类型字段
			event.action = dataInput.readInt();
			if (event.action == -1)
				event.action = null;
			event.intent = dataInput.readUTF();
			// event.result = dataInput.readUTF(); //新版本事件属性已改变类型字段
			event.result = dataInput.readInt();
			if (event.result == -1)
				event.result = null;
			event.sAddr = dataInput.readUTF();
			// event.sName = dataInput.readUTF(); //新版本事件属性已取消字段
			event.sPort = dataInput.readInt();
			if (event.sPort == -1)
				event.sPort = null;
			// event.sProcess = dataInput.readUTF(); //新版本事件属性已取消字段
			// event.sUserID = dataInput.readUTF(); //新版本事件属性已取消字段
			event.sUserName = dataInput.readUTF();
			event.stAddr = dataInput.readUTF();
			event.stPort = dataInput.readInt();
			if (event.stPort == -1)
				event.stPort = null;
			event.dAddr = dataInput.readUTF();
			// event.dName = dataInput.readUTF(); //新版本事件属性已取消字段
			event.dPort = dataInput.readInt();
			if (event.dPort == -1)
				event.dPort = null;
			// event.dProcess = dataInput.readUTF(); //新版本事件属性已取消字段
			// event.dUserID = dataInput.readUTF(); //新版本事件属性已取消字段
			event.dUserName = dataInput.readUTF();
			event.dtAddr = dataInput.readUTF();
			event.dtPort = dataInput.readInt();
			if (event.dtPort == -1)
				event.dtPort = null;
			event.devAddr = dataInput.readUTF();
			event.devName = dataInput.readUTF();
			event.devCategory = dataInput.readInt();
			if (event.devCategory == -1)
				event.devCategory = null;
			// event.devType = dataInput.readUTF(); //新版本事件属性已改变类型字段
			event.devType = dataInput.readInt();
			if (event.devType == -1)
				event.devType = null;
			event.devVendor = dataInput.readUTF();
			event.programType = dataInput.readUTF();
			// event.program = dataInput.readUTF(); //新版本事件属性已取消字段
			event.requestURI = dataInput.readUTF();
			event.name = dataInput.readUTF();// <------------摘要
			event.customS1 = dataInput.readUTF();
			event.customS2 = dataInput.readUTF();// <-----------备用字符串2
			event.customS3 = dataInput.readUTF();
			event.customS4 = dataInput.readUTF();// <-------备用字符串4
			event.customD1 = dataInput.readLong();
			if (event.customD1 == -1)
				event.customD1 = null;
			event.customD2 = dataInput.readLong();
			if (event.customD2 == -1)
				event.customD2 = null;
			event.devProduct = dataInput.readUTF();
			event.sessionID = dataInput.readUTF();
			// event.customS5 = dataInput.readUTF(); //新版本事件属性已取消字段
			// event.customS6 = dataInput.readUTF(); //新版本事件属性已改变为sMAC
			event.sMAC = dataInput.readUTF();
			// event.customS7 = dataInput.readUTF(); //新版本事件属性已改变为dMAC
			event.dMAC = dataInput.readUTF();
			event.customD3 = dataInput.readDouble();
			if (event.customD3 == -1)
				event.customD3 = null;
			event.customD4 = dataInput.readDouble();
			if (event.customD4 == -1)
				event.customD4 = null;
			// event.msg = dataInput.readUTF(); //新版本事件属性已取消字段
		} catch (IOException e) {
			logger.info("Read simevent: " + e.getMessage());
			return null;
		}
		return event;
	}

	/**
	 * 从数据buffer中读取事件。
	 * 
	 * @return SIMEventObject
	 */
	public SIMAlertObject readSIMAlert(byte[] data, int length) {
		return null;
	}

}

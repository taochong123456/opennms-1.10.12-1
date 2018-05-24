package org.opennms.netmgt.syslogd.parse;
import java.io.Serializable;
public class SIMEventObject implements Serializable, Cloneable {
	public long ID = 0; // 
	public long receptTime; // 
	public long aggregatedCount;// 
	public int sysType; // 
	public String collectorAddr = " "; //ַ [new]
	// public byte[] collectorAddr; //ַ
	public String collectorName = " "; //� [new]
	// ֶ��ɽ��ֶܷ־��
	public int collectType; // ��ȡ��ʽ: 1��syslog��2��snmp trap��3��file��4��jdbc
	public String name = " ";// �¼
	public Integer category; // �¼

	public Integer type; // �¼ͣ־��

	public Integer priority = null; // �ȼ�:0,1,2,3,4 , 4�.
	public Long rawID = null; // �¼�: �¼ı��,�ɲ�¼ϵͳ�.

	public Long occurTime = null; // �¼ʱ��: �¼�¼��ʱ��
	// ��------��ɾ�ֶ�------��
	// public Long sendTime=null;//�¼ʱ��,�¼�͵�ϵ�ʱ��,һ�occurTime. [new]

	public Long duration = null; // �¼ʱ�̣�

	public Long send = new Long(0); // ֽΪ�丳��ֵΪ0Է�ֹͳʱ�

	public Long receive = new Long(0); // ֽΪ�丳��ֵΪ0Է�ֹͳʱ�

	public Integer protocol = null; // [Э��]: tcp,

	public Integer appProtocol = null; // [Ӧ��Э��]��http,https,ftp,

	// ��------ʹµ------��
	// public String object=" "; // �: �˭
	public Integer object; // �: �˭

	// ��------ʹµ------��
	// public String policy=" "; // �: ʹ�õķ, [繥ķ,ķ]
	public Integer policy; // �: ʹ�õķ, [繥ķ,ķ]

	public String resource = " "; // ��Դ: Դ:�ļĿ¼̻�ӻ豸

	// ��------ʹµ------��
	// public String action=" "; // �: �,ֹͣ,�,�,��,ɾ,��,��,��Ȩ,��½
	public Integer action = null; // �: �,ֹͣ,�,�,��,ɾ,��,��,��Ȩ,��½

	public String intent = " "; // ��ͼ��Σ��⣬��Ϣ�... [new]

	// ��------ʹµ------��
	// public String result=" "; // : �ɹ�,ʧ��,�
	public Integer result; // : �ɹ�,ʧ��,�

	public String sAddr = " "; // Դ��ַ
	// public byte[] sAddr; // Դ��ַ

	// ��------��ɾ�ֶ�------��
	// public String sName=" "; // Դ

	public Integer sPort = null; // Դ�˿�

	// ��------��ɾ�ֶ�------��
	// public String sProcess=" "; // Դ�

	// ��------��ɾ�ֶ�------��
	// public String sUserID=" "; // Դ�û�ID

	public String sUserName = " "; // Դ�û�

	public String stAddr = " "; // Դ��ַת��
	// public byte[] stAddr; // Դ��ַת��

	public Integer stPort = null; // Դ��ַת˿�

	public String dAddr = " "; // Ŀ�ĵ�ַ
	// public byte[] dAddr; // Ŀ�ĵ�ַ

	// ��------��ɾ�ֶ�------��
	// public String dName=" "; // Ŀ

	public Integer dPort = null; // Ŀ�Ķ˿�

	// ��------��ɾ�ֶ�------��
	// public String dProcess=" "; // Ŀ�Ľ

	// ��------��ɾ�ֶ�------��
	// public String dUserID=" "; // Ŀû�ID

	public String dUserName = " "; // Ŀû�

	public String dtAddr = " "; // 
	// public byte[] dtAddr; // 

	public Integer dtPort = null; // Ŀ�ĵ�ַת˿�

	public String devAddr = " "; // ַ
	// public byte[] devAddr; // ַ

	public String devName = " "; // �豸

	public Integer devCategory = null; // �豸�

	// ��------ʹµ------��
	// public String devType=" "; // �豸�ͺ�
	public Integer devType = 0; // �豸�ͺ�

	public String devVendor = " "; // �豸�

	public String devProduct = " "; // Ʒ 20070809 zhuzhen

	public String programType = " "; // �͡Ϊ��ҵ�ơ�

	// ��------��ɾ�ֶ�------��
	// public String program=" "; // ��

	public String sessionID = " "; // �ỰID 20070809 zhuzhen

	public String requestURI = " "; // �URI [new]

	public String msg = " "; // �: �Ϣ��һ�дԭʼ��־��Ϣ

	public String customS1 = " "; // ַ�1 [new]

	public String customS2 = " "; // ַ�2 [new]

	public String customS3 = " "; // ַ�3 [new]

	public String customS4 = " "; // ַ�4 [new]

	// ��------��ɾ�ֶ�------��
	// public String customS5=" "; //ַ�5 [new] 20070809 zhuzhen

	// ��------ֵ��ֶ�------��
	// public String customS6=" "; //ַ�6 [new] 20070809 zhuzhen
	public String sMAC = " "; // ԴMAC��ַ [new] 20070809 zhuzhen

	// ��------ֵ��ֶ�------��
	// public String customS7=" "; //ַ�7 [new] 20070809 zhuzhen
	public String dMAC = " "; // Ŀ��MAC��ַ [new] 20070809 zhuzhen

	public Long customD1 = null; // 1 [new]

	public Long customD2 = null; // 2 [new]

	public Double customD3 = null; // 3 [new] 20070809 zhuzhen

	public Double customD4 = null; // 4 [new] 20070809 zhuzhen

	public long getID() {
		return ID;
	}

	public void setID(long id) {
		ID = id;
	}

	public long getReceptTime() {
		return receptTime;
	}

	public void setReceptTime(long receptTime) {
		this.receptTime = receptTime;
	}

	public long getAggregatedCount() {
		return aggregatedCount;
	}

	public void setAggregatedCount(long aggregatedCount) {
		this.aggregatedCount = aggregatedCount;
	}

	public int getSysType() {
		return sysType;
	}

	public void setSysType(int sysType) {
		this.sysType = sysType;
	}

	public String getCollectorName() {
		return collectorName;
	}

	public void setCollectorName(String collectorName) {
		this.collectorName = collectorName;
	}

	public int getCollectType() {
		return collectType;
	}

	public void setCollectType(int collectType) {
		this.collectType = collectType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Long getRawID() {
		return rawID;
	}

	public void setRawID(Long rawID) {
		this.rawID = rawID;
	}

	public Long getOccurTime() {
		return occurTime;
	}

	public void setOccurTime(Long occurTime) {
		this.occurTime = occurTime;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public Long getSend() {
		return send;
	}

	public void setSend(Long send) {
		this.send = send;
	}

	public Long getReceive() {
		return receive;
	}

	public void setReceive(Long receive) {
		this.receive = receive;
	}

	public Integer getProtocol() {
		return protocol;
	}

	public void setProtocol(Integer protocol) {
		this.protocol = protocol;
	}

	public Integer getAppProtocol() {
		return appProtocol;
	}

	public void setAppProtocol(Integer appProtocol) {
		this.appProtocol = appProtocol;
	}

	public int getObject() {
		return object;
	}

	public void setObject(int object) {
		this.object = object;
	}

	public int getPolicy() {
		return policy;
	}

	public void setPolicy(int policy) {
		this.policy = policy;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}

	public String getIntent() {
		return intent;
	}

	public void setIntent(String intent) {
		this.intent = intent;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public Integer getSPort() {
		return sPort;
	}

	public void setSPort(Integer port) {
		sPort = port;
	}

	public String getSUserName() {
		return sUserName;
	}

	public void setSUserName(String userName) {
		sUserName = userName;
	}

	public Integer getStPort() {
		return stPort;
	}

	public void setStPort(Integer stPort) {
		this.stPort = stPort;
	}

	public Integer getDPort() {
		return dPort;
	}

	public void setDPort(Integer port) {
		dPort = port;
	}

	public String getDUserName() {
		return dUserName;
	}

	public void setDUserName(String userName) {
		dUserName = userName;
	}

	public Integer getDtPort() {
		return dtPort;
	}

	public void setDtPort(Integer dtPort) {
		this.dtPort = dtPort;
	}

	public String getDevName() {
		return devName;
	}

	public void setDevName(String devName) {
		this.devName = devName;
	}

	public Integer getDevCategory() {
		return devCategory;
	}

	public void setDevCategory(Integer devCategory) {
		this.devCategory = devCategory;
	}

	public int getDevType() {
		return devType;
	}

	public void setDevType(int devType) {
		this.devType = devType;
	}

	public String getDevVendor() {
		return devVendor;
	}

	public void setDevVendor(String devVendor) {
		this.devVendor = devVendor;
	}

	public String getDevProduct() {
		return devProduct;
	}

	public void setDevProduct(String devProduct) {
		this.devProduct = devProduct;
	}

	public String getProgramType() {
		return programType;
	}

	public void setProgramType(String programType) {
		this.programType = programType;
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	public String getRequestURI() {
		return requestURI;
	}

	public void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getCustomS1() {
		return customS1;
	}

	public void setCustomS1(String customS1) {
		this.customS1 = customS1;
	}

	public String getCustomS2() {
		return customS2;
	}

	public void setCustomS2(String customS2) {
		this.customS2 = customS2;
	}

	public String getCustomS3() {
		return customS3;
	}

	public void setCustomS3(String customS3) {
		this.customS3 = customS3;
	}

	public String getCustomS4() {
		return customS4;
	}

	public void setCustomS4(String customS4) {
		this.customS4 = customS4;
	}

	public String getSMAC() {
		return sMAC;
	}

	public void setSMAC(String smac) {
		sMAC = smac;
	}

	public String getDMAC() {
		return dMAC;
	}

	public void setDMAC(String dmac) {
		dMAC = dmac;
	}

	public Long getCustomD1() {
		return customD1;
	}

	public void setCustomD1(Long customD1) {
		this.customD1 = customD1;
	}

	public Long getCustomD2() {
		return customD2;
	}

	public void setCustomD2(Long customD2) {
		this.customD2 = customD2;
	}

	public Double getCustomD3() {
		return customD3;
	}

	public void setCustomD3(Double customD3) {
		this.customD3 = customD3;
	}

	public Double getCustomD4() {
		return customD4;
	}

	public void setCustomD4(Double customD4) {
		this.customD4 = customD4;
	}

	public String getCollectorAddr() {
		return collectorAddr;
	}

	public void setCollectorAddr(String collectorAddr) {
		this.collectorAddr = collectorAddr;
	}

	public String getSAddr() {
		return sAddr;
	}

	public void setSAddr(String addr) {
		sAddr = addr;
	}

	public String getStAddr() {
		return stAddr;
	}

	public void setStAddr(String stAddr) {
		this.stAddr = stAddr;
	}

	public String getDAddr() {
		return dAddr;
	}

	public void setDAddr(String addr) {
		dAddr = addr;
	}

	public String getDtAddr() {
		return dtAddr;
	}

	public void setDtAddr(String dtAddr) {
		this.dtAddr = dtAddr;
	}

	public String getDevAddr() {
		return devAddr;
	}

	public void setDevAddr(String devAddr) {
		this.devAddr = devAddr;
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}

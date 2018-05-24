package org.opennms.netmgt.collectd;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.protocols.jmx.connectors.Jsr160ConnectionWrapper;

public class HBaseDataCollector {
	public String resourceid = "-1";
	private Map<String, String> resultMap = new HashMap<String, String>();
	String[] threadMetrics = { "ThreadsTimedWaiting", "ThreadsWaiting",
			"ThreadsBlocked", "ThreadsRunnable" };
	String[] jvmMetrics = { "MemMaxM", "MemHeapUsedM", "MemHeapCommittedM",
			"GcCount" };
	String[] operatingSystem = { "FreePhysicalMemorySize", "FreeSwapSpaceSize",
			"TotalPhysicalMemorySize", "TotalSwapSpaceSize",
			"CommittedVirtualMemorySize" };
	String[] runTime = { "BootClassPath", "ClassPath", "SpecVendor",
			"SpecVersion", "VmName", "VmVendor" };
	private JMXConnector connector = null;
	private MBeanServerConnection mbeanSC = null;
	static ThreadCategory log = ThreadCategory
			.getInstance(HBaseDataCollector.class);

	public Map<String, String> startDatacollection(Jsr160ConnectionWrapper connection) {
		setConnection(connection);
		String typeOfNode = "";
		try {
			typeOfNode = discoverTypeOfNode();
			resultMap.put("typeOfNode", typeOfNode);
			getOverviewData();
			getPerformanceData();
		} catch (Exception exc) {
			log.error("HBaseDataCollector startDatacollection:" + exc);
		}
		return resultMap;
	}

	private void getOverviewData() {
		getAttributes("Hadoop:service=HBase,name=JvmMetrics",
				this.threadMetrics);
		getAttributes("Hadoop:service=HBase,name=JvmMetrics", this.jvmMetrics);
		getAttributes("java.lang:type=OperatingSystem", this.operatingSystem);
		getAttributes("java.lang:type=Runtime", this.runTime);
	}

	private void getPerformanceData() {
		try {
			String typeOfNode = resultMap.get("typeOfNode");

			String[] regionServerAttrs = { "metric_storeCount",
					"metric_storeFileCount", "metric_memStoreSize",
					"metric_storeFileSize", "metric_compactionsCompletedCount",
					"metric_numBytesCompactedCount",
					"metric_numFilesCompactedCount", "metric_deleteCount",
					"metric_incrementCount", "metric_appendCount",
					"metric_scanNext_num_ops", "metric_scanNext_min",
					"metric_scanNext_max", "metric_scanNext_mean",
					"metric_scanNext_median", "metric_mutateCount",
					"metric_get_num_ops", "metric_get_min", "metric_get_max",
					"metric_get_mean", "metric_get_median" };

			ArrayList regionServerAttrsList = new ArrayList(
					Arrays.asList(regionServerAttrs));

			String[] IPCAttributes = { "numOpenConnections",
					"TotalCallTime_num_ops", "numActiveHandler",
					"numCallsInReplicationQueue",
					"exceptions.OutOfOrderScannerNextException",
					"exceptions.UnknownScannerException",
					"exceptions.RegionTooBusyException", "sentBytes",
					"receivedBytes" };

			getAttributes("Hadoop:service=HBase,name=" + typeOfNode
					+ ",sub=IPC", IPCAttributes);

			if (typeOfNode.equals("Master")) {
				String[] regionServers = { "tag.liveRegionServers",
						"tag.deadRegionServers" };
				getTableAttributesForRegionServers(
						"Hadoop:service=HBase,name=Master,sub=Server",
						regionServers);

				String[] regionsInTransition = { "ritCount",
						"ritCountOverThreshold" };
				getAttributes(
						"Hadoop:service=HBase,name=Master,sub=AssignmentManger",
						regionsInTransition);

				String[] hLogMetrics = { "HlogSplitTime_mean",
						"HlogSplitTime_min", "HlogSplitTime_max",
						"HlogSplitTime_num_ops", "HlogSplitSize_mean",
						"HlogSplitSize_min", "HlogSplitSize_max",
						"HlogSplitSize_num_ops" };

				getAttributes(
						"Hadoop:service=HBase,name=Master,sub=FileSystem",
						hLogMetrics);

				String[] generalMasterMetrics = { "averageLoad",
						"numDeadRegionServers", "numRegionServers",
						"clusterRequests" };
				getAttributes("Hadoop:service=HBase,name=Master,sub=Server",
						generalMasterMetrics);
			} else if (typeOfNode.equals("RegionServer")) {
				String[] blockCacheMetrics = { "blockCacheCount",
						"blockCacheEvictionCount", "blockCacheFreeSize",
						"blockCacheExpressHitPercent", "blockCacheHitCount",
						"blockCacheCountHitPercent", "blockCacheMissCount",
						"blockCacheSize" };

				getAttributes(
						"Hadoop:service=HBase,name=RegionServer,sub=Server",
						blockCacheMetrics);

				String[] slowCount = { "slowAppendCount", "slowGetCount",
						"slowPutCount", "slowIncrementCount", "slowDeleteCount" };

				getAttributes(
						"Hadoop:service=HBase,name=RegionServer,sub=Server",
						slowCount);

				getAllAttributes(
						"Hadoop:service=HBase,name=RegionServer,sub=Regions",
						regionServerAttrsList);
			}
		} catch (Exception e) {
			log.error("HBaseDataCollector getPerformanceData:" + e);
		}
	}

	private void getTableAttributesForRegionServers(String objectName,
			String[] attrs) {
		try {
			ObjectName obj = new ObjectName(objectName);

			String[] colNames = { "HostNameRegionServer",
					"custom_row_availability", "StartTimeRegionServer" };
			ArrayList header = new ArrayList(Arrays.asList(colNames));

			AttributeList list = this.mbeanSC.getAttributes(obj, attrs);
			for (int i = 0; i < attrs.length; i++) {
				Attribute att = (Attribute) list.get(i);
				String nodes = att.getValue().toString();
				List nodesList = new ArrayList();
				if (!nodes.equals("")) {
					nodesList = Arrays.asList(nodes.split(";"));
				}
				for (int j = 0; j < nodesList.size(); j++) {
					String[] nodeDetails = ((String) nodesList.get(j))
							.split(",");
					if (attrs[i].contains("live"))
						nodeDetails[1] = "1";
					else {
						nodeDetails[1] = "0";
					}
					long startTimeMillis = Long.parseLong(nodeDetails[2]);
					Date startTime = new Date(startTimeMillis);
					nodeDetails[2] = startTime.toLocaleString();
					ArrayList arrList = new ArrayList(
							Arrays.asList(nodeDetails));
				}
			}
		} catch (Exception exe) {
			log.error("HBaseDataCollector getTableAttributesForRegionServers:"
					+ exe);
		}
	}

	private AttributeList getAttributes(String objectName, String[] attrs) {
		AttributeList list = null;
		try {
			ObjectName obj = new ObjectName(objectName);
			list = this.mbeanSC.getAttributes(obj, attrs);
			for (int i = 0; i < attrs.length; i++) {
				Attribute att = (Attribute) list.get(i);
				if(att!=null){
					this.resultMap.put(attrs[i], att.getValue().toString());
				}
			}
		} catch (Exception e) {
			log.error("HBaseDataCollector getAttributes:" + e);
		}
		return list;
	}

	private MBeanInfo getAllAttributes(String objectName,
			ArrayList<String> attrs) {
		MBeanInfo list = null;
		try {
			ObjectName obj = new ObjectName(objectName);
			list = this.mbeanSC.getMBeanInfo(obj);

			String attributeName = null;
			String shortAttrName = null;

			MBeanAttributeInfo[] attributesMetadata = list.getAttributes();
			for (MBeanAttributeInfo attributeMetadata : attributesMetadata) {
				attributeName = attributeMetadata.getName();
				shortAttrName = attributeName.substring(attributeName
						.indexOf("metric_") != -1 ? attributeName
						.indexOf("metric_") : 0);
				if (attrs.contains(shortAttrName)) {
					String attributeValue = this.mbeanSC.getAttribute(obj,
							attributeName).toString();
					this.resultMap.put(shortAttrName, attributeValue);
				}
			}
		} catch (Exception e) {
			log.error("HBaseDataCollector getAllAttributes:" + e);
		}
		return list;
	}

	private Object getValue(MBeanServerConnection mbeanSC, ObjectName oname,
			String attrName) {
		Object toRet = null;
		try {
			toRet = this.mbeanSC.getAttribute(oname, attrName);
		} catch (Exception ee) {
			log.error("HBaseDataCollector getValue:" + ee);
		}
		return toRet;
	}

	public String discoverTypeOfNode() {
		String typeOfNode = null;
		try {
			ObjectName objName = new ObjectName(
					"Hadoop:service=HBase,name=JvmMetrics");
			typeOfNode = (String) getValue(this.mbeanSC, objName,
					"tag.ProcessName");
		} catch (MalformedObjectNameException e) {
			log.error("HBaseDataCollector discoverTypeOfNode:" + e);
		}
		return typeOfNode;
	}

	public void setConnection(Jsr160ConnectionWrapper connection) {
		connector = connection.getConnector();
		mbeanSC = connection.getMBeanServer();
	}
	public Map<String, String> getResultMap(){
		return resultMap;
	}
	
}
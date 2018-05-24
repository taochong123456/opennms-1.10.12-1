package org.opennms.netmgt.collectd;

import java.util.ArrayList;

public class HadoopConstants {
	public static final String JOB_TRACKER = "JobTracker";
	public static final String NAME_NODE = "NameNode";
	public static String jmx_url = "/jmx";
	public static String apps_url = "/ws/v1/cluster/metrics";

	public static String mbean_blocks_stat_oname = "Hadoop:service=NameNode,name=FSNamesystemMetrics";
	public static String mbean_dfs_stat_oname = "Hadoop:service=NameNode,name=NameNodeInfo";
	public static String mbean_nnfsstate_oname = "Hadoop:service=NameNode,name=FSNamesystemState";
	public static String mbean_nnjvm_stat_oname = "Hadoop:service=NameNode,name=jvm";
	public static String mbean_nnos_stat_oname = "java.lang:type=OperatingSystem";
	public static String mbean_jtnode_stat_oname = "Hadoop:service=JobTracker,name=JobTrackerInfo";
	public static String mbean_job_stat_oname = "Hadoop:service=JobTracker,name=JobTrackerMetrics";

	public static String mbean_v2_blocks_stat_oname = "Hadoop:service=NameNode,name=FSNamesystem";
	public static String mbean_rm_stat_oname = "Hadoop:service=ResourceManager,name=RMNMInfo";
	public static String mbean_nm_stat_oname = "Hadoop:service=ResourceManager,name=ClusterMetrics";

	public static String[] dfs_stat_attrs = { "HostName", "Total",
			"NonDfsUsedSpace", "Used", "PercentUsed", "Free",
			"PercentRemaining" };
	public static String[] blocks_stat_attrs = { "FilesTotal", "TotalLoad",
			"UnderReplicatedBlocks", "PendingReplicationBlocks",
			"PendingDeletionBlocks", "ExcessBlocks", "CorruptBlocks",
			"MissingBlocks", "BlockCapacity", "BlocksTotal" };
	public static String[] nn_fsstate_attrs = { "FSState" };
	public static String[] nn_jvm_stat_attrs = { "memNonHeapUsedM",
			"memNonHeapCommittedM", "memHeapUsedM", "memHeapCommittedM" };
	public static String[] nn_os_stat_attrs = { "TotalPhysicalMemorySize",
			"FreePhysicalMemorySize", "TotalSwapSpaceSize",
			"FreeSwapSpaceSize", "MaxFileDescriptorCount",
			"OpenFileDescriptorCount", "SystemLoadAverage" };
	public static String[] jtnode_stat_attrs = { "Hostname", "SummaryJson",
			"slots" };
	public static String[] job_stat_attrs = { "jobs_submitted",
			"jobs_preparing", "jobs_running", "jobs_failed", "jobs_killed",
			"jobs_completed" };

	public static String[] v2_block_stat_attrs = { "BlockCapacity",
			"BlocksTotal", "MissingBlocks", "UnderReplicatedBlocks",
			"CorruptBlocks", "ExcessBlocks", "TotalLoad",
			"PendingReplicationBlocks", "PendingDeletionBlocks" };
	public static String[] rm_stat_attrs = { "LiveNodeManagers" };
	public static String[] nm_stat_attrs = { "NumActiveNMs",
			"NumDecommissionedNMs", "NumLostNMs", "NumUnhealthyNMs",
			"NumRebootedNMs" };
	public static String[] v2_dfs_stat_attrs = { "ClusterId", "Total",
			"NonDfsUsedSpace", "Used", "Free", "PercentUsed",
			"PercentRemaining", "LiveNodes", "DeadNodes",
			"PercentBlockPoolUsed" };
	public static String[] v2_dn_stats_attrs = { "FSState", "NumLiveDataNodes",
			"NumDeadDataNodes", "NumDecomLiveDataNodes",
			"NumDecomDeadDataNodes", "NumDecommissioningDataNodes",
			"NumStaleDataNodes", "FilesTotal" };
	public static String[] app_stat_attrs = { "appsSubmitted", "appsCompleted",
			"appsPending", "appsRunning", "appsFailed", "appsKilled",
			"totalMB", "reservedMB", "availableMB", "allocatedMB" };
	public static final String LIVE = "Live";
	public static final String DEC_IN_PROGRESS = "Decommission In Progress";
	public static final String LIVE_DEC = "Live - Decommissioned";
	public static final String DEAD_DEC = "Dead - Decommissioned";
	public static final String DEAD = "Dead";
	public static final String DEC = "Decommissioned";
	public static final String ALIVE = "Alive";
	public static final String BLACKLISTED = "Blacklisted";
	public static final String GRAYLISTED = "Graylisted";

	public static ArrayList<String> getDataNodeAttributes() {
		ArrayList datanodeAttr = new ArrayList();
		try {
			datanodeAttr.add("NodeName");
			datanodeAttr.add("State");
			datanodeAttr.add("Usedspace");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return datanodeAttr;
	}

	public static ArrayList<String> getTaskTrackerlist() {
		ArrayList taskTrackerAttr = new ArrayList();
		try {
			taskTrackerAttr.add("TrackerName");
			taskTrackerAttr.add("State");
			taskTrackerAttr.add("HealthStatus");
			taskTrackerAttr.add("Failures");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return taskTrackerAttr;
	}

	public static ArrayList<String> getQueueArrtibutes() {
		ArrayList queueAttr = new ArrayList();
		try {
			queueAttr.add("QueueName");
			queueAttr.add("State");
			queueAttr.add("Info");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return queueAttr;
	}

	public static ArrayList<String> getV2DataNodeAttributes() {
		ArrayList dataNodeAttr = new ArrayList();
		try {
			dataNodeAttr.add("v2dataNodeHostName");
			dataNodeAttr.add("v2dataNodeState");
			dataNodeAttr.add("v2dataNodeCapacity");
			dataNodeAttr.add("v2dataNodeNonDfsUsedSpace");
			dataNodeAttr.add("v2dataNodeUsedSpace");
			dataNodeAttr.add("v2dataNodeRemaining");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataNodeAttr;
	}

	public static ArrayList<String> getV2NodeManagerAttributes() {
		ArrayList dataNodeAttr = new ArrayList();
		try {
			dataNodeAttr.add("HostName");
			dataNodeAttr.add("Rack");
			dataNodeAttr.add("State");
			dataNodeAttr.add("NodeManagerVersion");
			dataNodeAttr.add("MemoryUsedPercent");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataNodeAttr;
	}
}
package org.opennms.netmgt.syslogd.analyze;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
public class SIMRuleObject implements java.io.Serializable{	

	private String ruleID;// 规则编号
	
	private String name;//名字
	
	private boolean enabled ;//缺省停用
	
	private Map<String, SIMFilterObject> filterMaps = new LinkedHashMap<String, SIMFilterObject>();//规则内部使用的全部过滤器定义，其中可能有一个过滤器包含关联信息。
	
	private TimeScope timeScope;//事件发生频率定义
	
	private TimeScope fireInterval;//相邻预警的触发时间间隔
	
	private VarBinds patternVar;//关联过滤器引用，可以为null。实际的过滤器定义在filterMaps中
	
	private List<VarBinds> pointVars ;//定义的基本过滤器引用，pointVars的size至少为1。每个元素描述的都是单事件过滤器引用。实际的过滤器定义在filterMaps中
	
	private List<VarBinds> idVars ;//重复事件相同属性的描述
	
	private List<VarBinds> diffVars ;//重复事件不同属性的描述
	
	private List<MActionObject> actions;//定义的事件动作
	
	/**
	 * 构造函数	
	 */
	public SIMRuleObject() {

	}
	/**
	 * 构造函数
	 * @param ruleID
	 */
	public SIMRuleObject(String ruleID) {
		this.ruleID = ruleID;
	}
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}	

	public String getRuleID() {
		return ruleID;
	}

	public void setRuleID(String ruleID) {
		this.ruleID = ruleID;
	}
	public List<MActionObject> getActions() {
		return actions;
	}
	public void setActions(List<MActionObject> actions) {
		this.actions = actions;
	}
	public List<VarBinds> getDiffVars() {
		return diffVars;
	}
	public void setDiffVars(List<VarBinds> diffVars) {
		this.diffVars = diffVars;
	}
	public Map<String, SIMFilterObject> getFilterMaps() {
		return filterMaps;
	}
	public void setFilterMaps(Map<String, SIMFilterObject> filterMaps) {
		this.filterMaps = filterMaps;
	}
	public List<VarBinds> getIdVars() {
		return idVars;
	}
	public void setIdVars(List<VarBinds> idVars) {
		this.idVars = idVars;
	}
	public VarBinds getPatternVar() {
		return patternVar;
	}
	public void setPatternVar(VarBinds patternVar) {
		this.patternVar = patternVar;
	}
	public List<VarBinds> getPointVars() {
		return pointVars;
	}
	public void setPointVars(List<VarBinds> pointVars) {
		this.pointVars = pointVars;
	}
	public TimeScope getTimeScope() {
		return timeScope;
	}
	public void setTimeScope(TimeScope timeScope) {
		this.timeScope = timeScope;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public TimeScope getFireInterval() {
		return fireInterval;
	}
	public void setFireInterval(TimeScope fireInterval) {
		this.fireInterval = fireInterval;
	}

}

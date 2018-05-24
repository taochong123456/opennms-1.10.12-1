package org.opennms.netmgt.syslogd.analyze;
public class SIMFilterObject implements java.io.Serializable {

	/**
	 * 过滤器对应的ID
	 */
	private String id;
	/**
	 * 过滤器对应的名字
	 */
	private String name;
	/**
	 * 表达式的根节点.
	 */
	private ExpressionNode root;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ExpressionNode getRoot() {
		return root;
	}

	public void setRoot(ExpressionNode root) {
		this.root = root;
	}

}

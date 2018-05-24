package org.opennms.netmgt.syslogd.analyze;

import java.util.List;

public class ExpressionNode implements java.io.Serializable {
	/**
	 * 逻辑判断类型可以为'not','and','or','base'.其中base表示不可再分的逻辑表达式.
	 */
	String type;
/**
	 * 逻辑运算符，他们在type为'base'时起作用，目前包括：
	 * eq   等于,即'='
	 * ne   不等于,即'!='
	 * lt   小于,即'<'
	 * le   小于等于,即'<='
	 * gt   大于,即'>'
	 * ge   大于等于,即'>='
	 * between 在两者之间
	 * innumber 属于,即属于一组数值
	 
	 * eq_str 相等,即字符串相等
	 * eq_str_ic 相等,即字符串相等,忽略大小写
	 * in   属于,即属于一组字符串
	 * in_ic  属于,即属于一组字符串,忽略大小写	 	
	 * startswith 开始于
	 * startswith_ic 开始于,忽略大小写
	 * endswith 结束于
	 * endswith_ic 结束于,忽略大小写
	 * matches  匹配,即正则表达式匹配
	 * matches_ic 匹配,即正则表达式匹配,忽略大小写
	 * contains  包含,对于A contains B,B可以是一个字符串长量或正则表达式
	 * contains_ic 包含,对于A contains B,B可以是一个字符串长量或正则表达式,忽略大小写
	 
	 * insubnet 属于子网,
	 * incategory 属于分类,
	 * isnull 是否为空, 对应的值为yes或no
	 * 具体参见{@link secfox.soc.commons.data.filter.Constants Constants}
	 */
	String opr;
	/**
	 * 逻辑运算符需要的操作数,他们在type为'base'时起作用.表示左操作数.
	 */
	VarBinds left;
	/**
	 * 逻辑运算符需要的操作数,他们在type为'base'时起作用.表示右操作数.
	 */
	VarBinds right;

	/**
	 * 子逻辑表达式,他们在type不为'base'时起作用,这些子表达式的关系由type的值决定
	 */
	List<ExpressionNode> children;

	public List<ExpressionNode> getChildren() {
		return children;
	}

	public void setChildren(List<ExpressionNode> children) {
		this.children = children;
	}

	public String getOpr() {
		return opr;
	}

	public void setOpr(String opr) {
		this.opr = opr;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public VarBinds getLeft() {
		return left;
	}

	public void setLeft(VarBinds left) {
		this.left = left;
	}

	public VarBinds getRight() {
		return right;
	}

	public void setRight(VarBinds right) {
		this.right = right;
	}
}
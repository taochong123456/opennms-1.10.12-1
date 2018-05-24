/**
 * 版权所有 ( c ) 北京网御神州科技有限公司 2006。保留所有权利。
 *
 * 项目：	     manageserver
 * 文件名：	【LogicLibrary.java】
 * 描述：	    【管理服务器-公共管理组件层-审计部分-过滤器管理-逻辑运算类】
 * 作者名：	【朱震】
 * 日期：	    【2006-07-10】
 * $Id: LogicUnit.java,v 1.1 2008/11/06 08:39:21 liunan Exp $
 * 修改历史：
 */
package org.opennms.netmgt.syslogd.logic;

abstract public class LogicUnit {
	/**
	 * 转换成浮点数
	 * 
	 * @param operand
	 * @return
	 */
	public double transToDouble(Object operand) {
		if (operand instanceof String)
			return Double.parseDouble((String) operand);
		else if (operand instanceof Number) {
			return ((Number) operand).doubleValue();
		} else
			throw new IllegalArgumentException("illegal number argument ["
					+ operand + "]when calculating logic expression.");
	}

	/**
	 * 转换成长整数数
	 * 
	 * @param operand
	 * @return
	 */
	public long transToLong(Object operand) {
		if (operand instanceof String)
			return Long.parseLong((String) operand);
		else if (operand instanceof Number) {
			return ((Number) operand).longValue();
		} else
			throw new IllegalArgumentException("illegal number argument ["
					+ operand + "]when calculating logic expression.");
	}

	/**
	 * 判断
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
	abstract public boolean judge(Object left, Object right);
}

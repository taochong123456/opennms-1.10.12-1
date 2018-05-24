package org.opennms.netmgt.syslogd.logic;
public class EQLogic extends LogicUnit {
	@Override
	/**
	 * 等于比较。该函数目前具有局限性，对Float与Double类型之间的比较存在误差。
	 */
	public boolean judge(Object left, Object right) {
		if(left==null || right ==null)
			return false;
		
		double leftD =transToDouble(left);		
		double rightD = transToDouble(right);		
		return leftD==rightD;
	}
	
	public static void main(String[] args) {
		EQLogic eql = new EQLogic();
		System.out.println(eql.judge(new Double(45.01), "45.01"));
		System.out.println(null==null);
	}
}

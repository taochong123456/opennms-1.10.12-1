package org.opennms.netmgt.syslogd.logic;

public class InnumberLogic extends LogicUnit {

	@Override
	/**
	 * 离散数据集和判断，判断left的值是否在right组成的集合中。
	 * 该函数目前具有局限性，对Float与Double类型之间的比较存在误差。
	 */
	public boolean judge(Object left, Object right) {
		if (left == null || right == null)
			return false;

		double leftD = transToDouble(left);

		if (right instanceof Object[]) {
			Object[] inParams = (Object[]) right;
			for (int i = 0; i < inParams.length; i++) {
				double element = transToDouble(inParams[i]);
				if (leftD == element)
					return true;
			}
			return false;
		} else
			throw new IllegalArgumentException(
					"illegal  innumber params. Need Object[]");
	}

	public static void main(String[] args) {
		InnumberLogic bl = new InnumberLogic();
		// System.out.println(bl.judge(23, new Object[]{"33",45,"23"}));
		System.out.println(bl.judge(23, null));
	}

}

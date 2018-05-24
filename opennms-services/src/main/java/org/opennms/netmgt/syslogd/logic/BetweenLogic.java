package org.opennms.netmgt.syslogd.logic;

public class BetweenLogic extends LogicUnit {

	@Override
	/**
	 * 范围之间比较。闭区间比较，该闭区间的值在right参数中。
	 * 
	 * 该函数目前具有局限性，对Float与Double类型之间的比较存在误差。
	 */
	public boolean judge(Object left, Object right) {

		if (left == null || right == null)
			return false;

		double leftD = transToDouble(left);

		if (right instanceof Object[]) {

			Object[] betweenParams = (Object[]) right;
			if (betweenParams.length < 2)
				throw new IllegalArgumentException("Short of  between params.");

			double leftRange = transToDouble(betweenParams[0]);
			double rightRange = transToDouble(betweenParams[1]);

			if (leftRange <= leftD && leftD <= rightRange)
				return true;
			else
				return false;

		} else
			throw new IllegalArgumentException(
					"illegal  between params. Need Object[]");

	}

	public static void main(String[] args) {
		BetweenLogic bl = new BetweenLogic();
		System.out.println(bl.judge(40, new Object[] { "33", 45, "23" }));
	}

}

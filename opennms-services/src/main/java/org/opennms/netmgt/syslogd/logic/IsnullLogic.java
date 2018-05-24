package org.opennms.netmgt.syslogd.logic;

public class IsnullLogic extends LogicUnit {
	/**
	 * 判断是否为空。
	 * 
	 * @param param
	 * @return
	 */
	private boolean isNull(Object param) {
		if (param == null)
			return true;
		else {
			if ((param instanceof String)
					&& ((String) param).trim().length() == 0)// 空字符串也视为null
				return true;
			else
				return false;
		}
	}

	@Override
	public boolean judge(Object left, Object right) {
		int flag = 1;
		if (right != null) {
			if (right.toString().equalsIgnoreCase("yes")
					|| right.toString().equalsIgnoreCase("y"))
				flag = 1;
			else
				flag = 0;
		}

		if (flag == 1) {
			if (isNull(left))
				return true;
			else
				return false;
		} else {
			if (isNull(left))
				return false;
			else
				return true;
		}
	}

	public static void main(String[] args) {
		IsnullLogic inl = new IsnullLogic();
		System.out.println(inl.judge(null, "y"));

	}
}

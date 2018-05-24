package org.opennms.netmgt.syslogd.analyze;
import java.io.File;
import java.util.*;

/**
 * 字符串工具类
 */
public class StringTool {

	/**
	 * 校验ip字符串的合法性。如127.0.0.1等
	 * 
	 * @param ipString
	 *            String
	 * @return boolean
	 */
	public static boolean validIP(String ipString) {
		if (ipString == null || ipString.trim().length() == 0) { // ||ipString.endsWith("."))
																	// {
			return false;
		}
		String[] ipField = ipString.split("\\x2e");
		if (ipField == null || ipField.length != 4) {
			return false;
		}
		try {
			int ipFiled0 = Integer.parseInt(ipField[0]);
			int ipFiled1 = Integer.parseInt(ipField[1]);
			int ipFiled2 = Integer.parseInt(ipField[2]);
			int ipFiled3 = Integer.parseInt(ipField[3]);
			if (ipFiled0 < 1 || ipFiled0 > 223) { // 最高段
				return false;
			}
			if (ipFiled1 < 0 || ipFiled1 > 255) {
				return false;
			}
			if (ipFiled2 < 0 || ipFiled2 > 255) {
				return false;
			}
			if (ipFiled3 < 0 || ipFiled3 > 255) {
				return false;
			}
		} catch (NumberFormatException ex) {
			return false;
		}
		return true;
	}

	/**
	 * 判断子网掩码的合法性
	 * 
	 * @param ipMask
	 *            String
	 * @return boolean
	 */
	public static boolean validMask(String ipMask) {
		boolean endSubnet = false;

		if (ipMask == null || ipMask.trim().length() == 0) {
			return false;
		}
		String[] ipField = ipMask.split("\\x2e"); // 按照小数点分割字符串
		if (ipField == null || ipField.length != 4) {
			return false;
		}

		int[] f = new int[4];
		try {
			f[0] = Integer.parseInt(ipField[0]);
			f[1] = Integer.parseInt(ipField[1]);
			f[2] = Integer.parseInt(ipField[2]);
			f[3] = Integer.parseInt(ipField[3]);
			if (f[0] < 128 || f[0] > 255) { // 最高段
				return false;
			}
			if (f[1] < 0 || f[1] > 255) {
				return false;
			}
			if (f[2] < 0 || f[2] > 255) {
				return false;
			}
			if (f[3] < 0 || f[3] > 255) {
				return false;
			}
		} catch (NumberFormatException ex) {
			return false;
		}

		int m = 0; // 余数

		for (int j = 3; j >= 0; j--) {
			for (int i = 0; i < 8; i++) {
				m = f[j] % 2;
				if (m == 1) { // 从右往左找到第一个1，
					endSubnet = true;
				} else {
					if (endSubnet == true) { // 从右往左找到第一个1后，又遇到一个0
						return false;
					}
				}
				f[j] = f[j] >> 1;

			}
		}

		return true;

	}

	/**
	 * 获取子网掩码0的个数，使用前应先通过validMask校验掩码的合法性。返回-1表示出错
	 * 
	 * @param ipMask
	 *            String
	 * @return int
	 */
	public static int getMaskZeros(String ipMask) {
		int count = 0;

		String[] ipField = ipMask.split("\\x2e"); // 按照小数点分割字符串

		int[] f = new int[4];
		try {
			f[0] = Integer.parseInt(ipField[0]);
			f[1] = Integer.parseInt(ipField[1]);
			f[2] = Integer.parseInt(ipField[2]);
			f[3] = Integer.parseInt(ipField[3]);
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
			return -1;
		}
		for (int i = 3; i >= 0; i--) {
			for (int j = 0; j < 8; j++) {
				if (f[i] % 2 == 0) {
					count++;
					f[i] /= 2;
				} else {
					return count;
				}
			}
		}

		return count;
	}

	public static boolean validSubNet(String ip, String ipMask) {

		if (!validIP(ip)) {
			return false;
		}
		if (!validMask(ipMask)) {
			return false;
		}
		int rightCount = getMaskZeros(ipMask);
		int count = 0;
		// ----------
		String[] ipField = ip.split("\\x2e"); // 按照小数点分割字符串
		int[] f = new int[4];
		try {
			f[0] = Integer.parseInt(ipField[0]);
			f[1] = Integer.parseInt(ipField[1]);
			f[2] = Integer.parseInt(ipField[2]);
			f[3] = Integer.parseInt(ipField[3]);
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
			return false;
		}
		for (int i = 3; i >= 0; i--) {
			for (int j = 0; j < 8; j++) {
				if (f[i] % 2 == 0) {
					count++;
					f[i] /= 2;
				} else if (count < rightCount) {
					return false;
				}
			}
		}
		// ----------
		return true;

	}

	/**
	 * 判断是否为中文
	 * 
	 * @param target
	 *            String
	 * @param encode
	 *            String
	 * @return boolean
	 */
	public static boolean isgb(String target) {
		byte[] bytes = new byte[2];
		int byteFrom, byteTo;
		byteFrom = 10 - 96;
		byteTo = 94 - 96;
		boolean check = true;
		try {
			bytes = target.getBytes("UTF-8");
		} catch (java.io.UnsupportedEncodingException e) {
			// System.out.println("getbyte error");
		}
		// if it isn't gb
		if ((bytes[0] < byteFrom) || (bytes[0] > byteTo)) {
			check = false;
		}
		return check;
	}

	/**
	 * 简单判断每级域名的合法性
	 * 
	 * @param field
	 *            String
	 * @return boolean
	 */
	public static boolean isValidDNSField(String field) {
		if (field == null || field.trim().length() == 0) {
			return false;
		}

		boolean isValidate = true;
		// if (field.length()>26)
		// return false;
		if (!Character.isLetterOrDigit(field.charAt(0))) { // 首字符必须为字母或数字
			return false;
		}
		// 定义合法字符
		String legalStr = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFJHIJKLMNOPQRSTUVWXYZ-_.";
		for (int i = 0; i < field.length(); i++) {
			char aChar = field.charAt(i);
			if (legalStr.indexOf(aChar) < 0) { // 如果字符不合法
				return false;
			}
		}
		return isValidate;

	}

	/**
	 * 判断DNS的合法性
	 * 
	 * @param src
	 *            String
	 * @return boolean
	 */
	public static boolean isValidDNSName_Simple(String src) {
		if (src == null | src.trim().length() == 0) {
			return false;
		}
		boolean result = true;
		int length = src.length();
		int bPosition = 0;
		int cPosition = 0;
		String field = "";
		while (bPosition <= length) {
			cPosition = src.indexOf('.', bPosition);
			if (cPosition != -1) {
				if (bPosition <= cPosition) {
					field = src.substring(bPosition, cPosition);
					result = isValidDNSField(field);
					if (result == false) {
						return result;
					}
					// System.out.println("field:" + field);
					// System.out.println("cPosition:" + cPosition);
				}
				bPosition = cPosition + 1;
			} else {
				if (bPosition <= length) {
					field = src.substring(bPosition, length);
					result = isValidDNSField(field);
					if (result == false) {
						return result;
					}
					// System.out.println("field:" + field);
					// System.out.println("cPosition:" + cPosition);
				}
				break;
			}

		}
		return result;
	}

	/**
	 * 对Email地址进行的简单合法性校验
	 * 
	 * @param src
	 *            String
	 * @return boolean
	 */
	// public static boolean isValidEmailName_Simple(String src) {
	// if (src == null | src.trim().length() == 0) {
	// return false;
	// }
	// String[] field = src.split("@");
	// if (field.length != 2) {
	// return false;
	// }
	// if (!isValidDNSName_Simple(field[0])) {
	// return false;
	// }
	// if (!isValidDNSName_Simple(field[1])) {
	// return false;
	// }
	// return true;
	//
	// }

	/**
	 * 校验邮件地址格式是否合法
	 * 
	 * @param mailAddress
	 *            String
	 * @return boolean
	 */
	public static boolean isValidEmailName_Simple(String mailAddress) {
		if (mailAddress.startsWith("@") || mailAddress.startsWith(".")) {
			return false;
		} else if (mailAddress.endsWith("@") || mailAddress.endsWith(".")) {
			return false;
		} else if (mailAddress.indexOf("@") == -1
				|| mailAddress.indexOf(".") == -1) {
			return false;
		} else if (mailAddress.indexOf("@") > mailAddress.indexOf(".")) {
			return false;
		} else if ((mailAddress.indexOf("@") + 1) == mailAddress.indexOf(".")) {
			return false;
		} else {
			return true;
		}
		// int indexAt = mailAddress.indexOf("@");
		// int indexDot = mailAddress.indexOf(".");
		// int indexAtNext = -1;
		//
		// if (indexAt != -1) {
		// indexAtNext = mailAddress.indexOf('@', indexAt + 1);
		// if ( (indexAtNext != -1) && (indexAtNext != indexAt)) {
		// return false;
		// }
		// }
		// int indexDotNext = mailAddress.indexOf('.', indexAt);
		// if ( (indexAt == -1 || indexDot == -1) || (indexAt > indexDot) ||
		// (mailAddress.endsWith("@")) || (mailAddress.endsWith("."))) {
		// return false;
		// }
		// return true;
	}

	/**
	 * 将本地串编码转到ISO编码
	 * 
	 * @param 本地字符编码的串
	 * @return ISO编码的串
	 */
	public static String getISOString(String str) {
		if (str == null || str.trim().length() == 0) {
			return "";
		}
		String strReturn = null;
		try {
			strReturn = new String(str.getBytes(), "ISO8859_1");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strReturn;
	}

	/**
	 * 将串从ISO转为本地串
	 * 
	 * @param ISO编码的字串
	 * @return 本地字串
	 */
	public static String getLocalStringFromISO(String str) {
		if (str == null || str.trim().length() == 0) {
			return "";
		}
		String strReturn = null;
		try {
			strReturn = new String(str.getBytes("ISO8859_1"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return strReturn;
	}

	/**
	 * 将串从ISO转为GB
	 * 
	 * @param ISO编码的字串
	 * @return GB编码的字串
	 */
	public static String getGBFromISO(String str) {
		String strReturn = "";
		if (str == null || str.trim().length() == 0) {
			return "";
		}
		try {
			strReturn = new String(str.getBytes("ISO8859_1"), "GB2312");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return strReturn;
	}

	// by zhuzhen
	/**
	 * 提取xxx/yyy/zzz.rrty.tyu7的zzz部分。
	 * 
	 * @param sourceString
	 * @return 抽取结果。
	 */
	public static String getFileMeaning(String sourceString) {
		String f = new File(sourceString).getName();
		// "\\x2e" 按"."分隔。
		String[] suff = f.split("\\x2e");
		return suff[0];
	}

	// by zhuzhen
	/**
	 * 提取xxx/yyy/zzz.rrty.tyu7的tyu7部分。
	 * 
	 * @param sourceString
	 * @return 抽取结果。
	 */
	public static String getFileExt(String sourceString) {
		String f = new File(sourceString).getName();
		// "\\x2e" 按"."分隔。
		String[] suff = f.split("\\x2e");
		if (suff.length == 1) {
			return ""; // 无扩展名
		}
		return suff[suff.length - 1];
	}

	/**
	 * 提取xxx/yyy/zzz.rrty.tyu7的".tyu7"部分。
	 * 
	 * @param sourceString
	 * @return 抽取结果。
	 */
	public static String getFileExtC(String sourceString) {
		String f = new File(sourceString).getName();
		// "\\x2e" 按"."分隔。
		String[] suff = f.split("\\x2e");
		if (suff.length == 1) {
			return ""; // 无扩展名
		}
		return "." + suff[suff.length - 1];
	}

	// by zhuzhen
	/**
	 * 将字符串组成一个长串。按等于和并且拼接。如：a = '1' and b = '2'
	 * 
	 * @param baseInformation
	 *            源串
	 * @return 结果串。
	 */
	public static String transBaseInformation(String[][] baseInformation) {
		String result = "";
		int i;

		for (i = 0; i < baseInformation.length - 1; i++) {

			result = baseInformation[i][0] + " = '"
					+ baseInformation[i][1].trim() + "' and ";

		}
		result = result + " " + baseInformation[i][0] + " = '"
				+ baseInformation[i][1].trim() + "'";
		return result;
	}

	// by zhuzhen
	/**
	 * 将字符串组成一个长串。按等于和并且拼接。如：a matches '*1*' and b matches '*2*'
	 * 
	 * @param baseInformation
	 *            源串
	 * @return 结果串。
	 */
	public static String transBaseInformationM(String[][] baseInformation) {
		String result = "";
		int i;

		for (i = 0; i < baseInformation.length - 1; i++) {

			result = baseInformation[i][0] + " matches '*"
					+ baseInformation[i][1].trim() + "*' and ";

		}
		result = result + " " + baseInformation[i][0] + " matches '*"
				+ baseInformation[i][1].trim() + "*'";
		return result;
	}

	// by zhuzhen
	/**
	 * 判断是否为模糊查询。
	 * 
	 * @param source
	 *            源串
	 */
	public static boolean getSearchStatus(String source) {
		if (source.indexOf("*") != -1) {
			return true;
		} else {
			return false;
		}
	}

	public static String longToIP(long l) {

		String sip = "";

		l = l & 0x00000000ffffffff;

		byte[] bip = new byte[4];

		bip[0] = (byte) ((l >> 24) & 0xff);

		bip[1] = (byte) ((l >> 16) & 0xff);

		bip[2] = (byte) ((l >> 8) & 0xff);

		bip[3] = (byte) (l & 0xff);

		for (int i = 0; i < bip.length; i++) {

			if (bip[i] >= 0)

				sip += bip[i];

			else

				sip += bip[i] + 256;

			if (i != 3)

				sip += ".";

		}

		return sip;

	}

	public static long ipToLong(String sip) {

		long ret = 0;

		int i = 3;

		StringTokenizer stk = new StringTokenizer(sip, ".");

		while (stk.hasMoreTokens()) {

			try {

				long n = Long.parseLong((String) stk.nextToken());

				if (n < 0 || n > 255) {

					return -1;

				}

				ret += (n << (i * 8));

			} catch (Exception e) {

				return -1;

			}

			i--;

		}

		if (i != -1)

			return -1;

		return ret;

	}

	/**
	 * 判断字符串是否含有内容。如果字符串为空或非空字符长度为0时返回false。
	 * 
	 * @param src
	 * @return
	 */
	public static boolean hasContent(String src) {
		if (src == null || src.trim().length() == 0)
			return false;
		else
			return true;
	}

	/**
	 * 将字符串转化为小写字母,并且去掉头尾空格。
	 * 
	 * @param src
	 * @return
	 */
	public static String toShrink(String src) {
		return src.toLowerCase().trim();
	}

	/**
	 * 测试。
	 */
	public static void main(String[] args) {
		// System.out.println(StringTool.getFileMeaning("asdf/asdf/eee"));
		// System.out.println(StringTool.transBaseInformationM(new
		// String[][]{{"rt","234523"},{"df","45"}}));
		// System.out.println(StringTool.getSearchStatus("*ad*fad*"));
		// System.out.println(StringTool.validIP("12.255.10.0"));
		// System.out.println(StringTool.validMask("254.0.0.0"));
		// System.out.println(isValidDNSName_Simple(" "));
		// System.out.println(validSubNet("10.1.1.2","255.255.255.0"));
		System.out.println(isValidEmailName_Simple("a.@con"));

	}
}

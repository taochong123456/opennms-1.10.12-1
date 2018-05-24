package org.opennms.netmgt.syslogd.analyze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringTools {

	private static final Map<String, Pattern> patterns = new HashMap<String, Pattern>();

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
	 * 提取形如name=value;name1=value1;name2=value1字符串中name对应的值,如果有多个相同的name,
	 * 则取第一个name的值。
	 * 
	 * @param source
	 *            消息串
	 * @param name
	 *            名字内容
	 * @return 名字内容对应的值,如果不存在返回null
	 */
	public static String getMessageValue(String source, String name) {
		if (name == null || name.trim().length() == 0 || source == null
				|| source.trim().length() == 0)
			return null;
		String[] segment = source.split(";");
		for (int i = 0; i < segment.length; i++) {
			String[] party = segment[i].split("=");
			if (party.length == 2) {
				if (party[0] != null && party[0].trim().equals(name)) {
					return party[1];
				}
			}
		}
		return null;
	}

	/**
	 * 获取指定匹配模式串对应的Pattern对象.并保证系统中相同的模式串,只对应一个Pattern对象实例.
	 * 
	 * @param regex
	 * @return
	 */
	public static Pattern getPattern(String regex) {
		Pattern workPattern = patterns.get(regex);
		if (workPattern == null) {
			workPattern = Pattern.compile(regex);
			patterns.put(regex, workPattern);
		}
		return workPattern;
	}

	/**
	 * 判断字符串是否包含指定的正规表达式模式. 可以重复使用此函数,比较高效.
	 * 
	 * @param source
	 *            源字符串
	 * @param regex
	 *            匹配模式
	 * @return
	 */
	public static boolean contains(String source, String regex) {
		Pattern p = getPattern(regex);
		Matcher m = p.matcher(source);
		if (m.find())
			return true;
		else
			return false;
	}

	/**
	 * 判断字符串是否完全匹配指定的正规表达式模式. 可以重复使用此函数,比较高效.
	 * 
	 * @param source
	 *            源字符串
	 * @param regex
	 *            匹配模式
	 * @return
	 */
	public static boolean matches(String source, String regex) {
		Pattern p = getPattern(regex);
		Matcher m = p.matcher(source);
		if (m.matches())
			return true;
		else
			return false;
	}

	/**
	 * 判断字符串是否是长整数。
	 * 
	 * @param number
	 * @return
	 */
	public static boolean isLongInteger(String number) {
		try {
			Integer.parseInt(number);
		} catch (Exception e) {
			try {
				Long.parseLong(number);
				return true;
			} catch (Exception ex) {
				return false;
			}

		}
		// 如果是普通整数，返回false;
		return false;
	}

	/**
	 * 判断字符串是否是数字,包括整数,小数。
	 * 
	 * @param number
	 * @return
	 */
	public static boolean isNumber(String number) {
		try {
			Long.parseLong(number.trim());
			return true;
		} catch (Exception e) {
			try {
				Double.parseDouble(number.trim());
				return true;
			} catch (Exception ex) {
				// TODO: handle exception
			}

		}
		return false;
	}

	/**
	 * 将字符true或false转化为boolean.
	 * 
	 * @param tag
	 * @param trend
	 *            倾向于转化的结果.即如果tag既不为true也不为false时转化的值.
	 * @return
	 */
	public static boolean transToboolean(String tag, boolean trend) {
		if (!hasContent(tag))
			return trend;
		tag = tag.trim();
		if (tag.equalsIgnoreCase("true"))
			return true;
		else if (tag.equalsIgnoreCase("false"))
			return false;
		else
			return trend;

	}

	/**
	 * 验证参数为2为数组。或第二维的长度至少为2。
	 * 
	 * @param params
	 *            二维参数列表。
	 * @return 如果参数合法，返回true,否则返回false。
	 */
	public static boolean validateArrayParams(String[][] params) {
		for (int i = 0; i < params.length; i++)
			if (params[i].length < 2) {
				return false;
			}
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
			System.out.println("getbyte error");
		}
		// if it isn't gb
		if ((bytes[0] < byteFrom) || (bytes[0] > byteTo)) {
			check = false;
		}
		return check;
	}

	/**
	 * 判断是否为中文或中文符号。
	 * 
	 * @param target
	 *            String
	 * @param encode
	 *            String
	 * @return boolean
	 */
	public static boolean isgb(byte[] bytes, int position) {
		int byteFrom, byteTo;
		// byteFrom = 10 - 96;
		byteFrom = 1 - 96;
		byteTo = 94 - 96;
		boolean check = true;

		// System.out.println(bytes[position]);
		// if it isn't gb
		if ((bytes[position] < byteFrom) || (bytes[position] > byteTo)) {
			check = false;
		}
		return check;
	}

	/**
	 * 判断指定字节处的汉字情况。
	 * 
	 * @param bytes
	 * @param postion
	 * @return 0:非汉字， 1:汉字的开始， 2：汉字的结束, -1：错误位置
	 */
	public static int getHzStatus(byte[] bytes, int postion) {
		if (bytes.length <= postion || postion < 0)
			return -1;

		int currentStatus = 0;
		for (int i = 0; i <= postion; i++) {
			boolean isHz = isgb(bytes, i);
			if (isHz) {
				if (currentStatus == 0 || currentStatus == 2)
					currentStatus = 1;
				else if (currentStatus == 1)
					currentStatus = 2;
			} else
				currentStatus = 0;
		}
		return currentStatus;
	}

	/**
	 * 将内容分段。
	 * 
	 * @param content
	 *            将被分段的内容
	 * @param standardLength
	 *            每段标准长度 [字节数]
	 * @param secondPrefix
	 *            第二段开始的提示语
	 * @return
	 */
	public static List<String> makeStringSegment(String content,
			int standardLength, String secondPrefix) {
		if (StringTools.hasContent(content)) {
			content = content.trim();

			byte[] bytes = content.getBytes();// 转换为字节

			int prefixLength = 0;// 第二段开始的前缀长度
			if (StringTools.hasContent(secondPrefix))
				prefixLength = secondPrefix.getBytes().length;

			List<String> segs = new ArrayList<String>();

			if (bytes.length <= standardLength) {// 不需要分段
				segs.add(content);
				return segs;
			}

			int currentPosition = 0;
			int nextPostion = 0;
			int segLength = standardLength;
			boolean isFirst = true;

			while (nextPostion < bytes.length) {
				currentPosition = nextPostion;
				nextPostion = currentPosition + segLength;
				if (StringTools.getHzStatus(bytes, nextPostion - 1) == 1) {// 如果最后一个字节是汉字的开始，则不应该截断[两个字节组成一个汉字]
					nextPostion--;
				}
				String current = (nextPostion < bytes.length) ? new String(
						bytes, currentPosition, nextPostion - currentPosition)
						: new String(bytes, currentPosition, bytes.length
								- currentPosition);
				segs.add(isFirst ? current : secondPrefix + current);
				if (isFirst) {
					isFirst = false;
					segLength = standardLength - prefixLength;
					if (segLength <= 1)
						break;
				}
			}

			return segs;

		}
		return null;
	}

	public void testDigest() {
		try {
			String myinfo = "我的测试信息";

			// java.security.MessageDigest
			// alg=java.security.MessageDigest.getInstance("MD5");
			java.security.MessageDigest alga = java.security.MessageDigest
					.getInstance("SHA-1");
			alga.update(myinfo.getBytes());
			byte[] digesta = alga.digest();
			System.out.println("本信息摘要是:" + byte2hex(digesta));
			// 通过某中方式传给其他人你的信息(myinfo)和摘要(digesta) 对方可以判断是否更改或传输正常
			java.security.MessageDigest algb = java.security.MessageDigest
					.getInstance("SHA-1");
			algb.update(myinfo.getBytes());
			if (algb.isEqual(digesta, algb.digest())) {
				System.out.println("信息检查正常");
			} else {
				System.out.println("摘要不相同");
			}

		} catch (java.security.NoSuchAlgorithmException ex) {
			System.out.println("非法摘要算法");
		}

	}

	public String byte2hex(byte[] b) // 二行制转字符串
	{
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1)
				hs = hs + "0" + stmp;
			else
				hs = hs + stmp;
			if (n < b.length - 1)
				hs = hs + ":";
		}
		return hs.toUpperCase();
	}

	public static final String hexToString(String hex) {
		if (hex == null)
			return "";
		int length = hex.length() & -4;
		StringBuffer result = new StringBuffer(length / 4);
		hexToString(hex, result);
		return result.toString();
	}

	public static final void hexToString(String hex, StringBuffer out) {
		if (hex == null)
			return;
		int length = hex.length() & -4;
		for (int pos = 0; pos < length; pos += 4) {
			int this_char = 0;
			try {
				this_char = Integer.parseInt(hex.substring(pos, pos + 4), 16);
			} catch (NumberFormatException NF_Ex) { /* dont care */
			}
			out.append((char) this_char);
		}
	}

	public static final String stringToHex(String java) {
		if (java == null)
			return "";
		int length = java.length();
		StringBuffer result = new StringBuffer(length * 4);
		stringToHex(java, result);
		return result.toString();
	}

	public static final void stringToHex(String java, StringBuffer out) {
		if (java == null)
			return;
		int length = java.length();
		for (int pos = 0; pos < length; pos++) {
			int this_char = (int) java.charAt(pos);
			for (int digit = 0; digit < 4; digit++) {
				int this_digit = this_char & 0xf000;
				this_char = this_char << 4;
				this_digit = (this_digit >> 12);
				if (this_digit >= 10)
					out.append((char) (this_digit + 87));
				else
					out.append((char) (this_digit + 48));
			}
		}
	}

	/**
	 * 获取形如"/aaa/bbb/ccc"的第一部分aaa
	 * 
	 * @param src
	 * @return
	 */
	public static String getFirstPart(String src) {
		if (src == null)
			return "";
		String[] segs = src.split("/");
		if (segs.length > 1)
			return segs[1];
		else
			return segs[0];
	}

	/**
	 * 截取获得指定长度的字符创
	 * 
	 * @param src
	 * @param length
	 * @return
	 */
	public static String getString(String src, int length) {
		if (src != null) {
			if (src.length() > length)
				return src.substring(0, length);
			else
				return src;
		} else {
			return "";
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

}

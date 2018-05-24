package org.opennms.netmgt.syslogd.parse;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class SysFunc {
	public static Set<String> SysVar = new HashSet<String>();
	static {
		System.setProperty("os_arch", System.getProperty("os.arch"));
		SysVar.add("os_arch");
		System.setProperty("os_name", System.getProperty("os.name"));
		SysVar.add("os_name");
		System.setProperty("os_version", System.getProperty("os.version"));
		SysVar.add("os_version");
		System.setProperty("user_language", System.getProperty("user.language"));
		SysVar.add("user_language");
	}
	private static SysFunc single = new SysFunc();

	private Random r = new Random();

	private SysFunc() {
	}

	static SysFunc getInstance() {
		return single;
	}

	public int test() {
		return 1;
	}

	/**
	 * ��ʽ���ַ�
	 * 
	 * @param format
	 * @param args
	 * @return
	 */
	public String format(String format, Object args) {
		String f = format.replace("@", "$");
		String[] pp = (String[]) args;
		return String.format(f, pp);
	}

	/**
	 * ����0��С��number֮���α�����
	 * 
	 * @param number
	 * @return
	 */
	public int random(int number) {
		return r.nextInt(number);
	}

	/**
	 * ������ת��ΪIP�ַ�
	 * 
	 * @param ipInt
	 * @return
	 */
	public String transToIP(int ipInt) {
		try {
			return "";
		} catch (Exception e) {
			return "";
		}

	}

	// Add by liyue 2012-2-28 Ϊɽ����������NBA_IA���ӵ�ַת������
	/**
	 * ���ַ�������ת��ΪIP�ַ�
	 * 
	 * @param ipInt
	 * @return
	 */
	public String transToIPForNBA_IA(String ipInt) {
		try {
			return "";
		} catch (Exception e) {
			return "";
		}

	}

	public static void main(String[] args) {
		// System.getProperties().list(System.out);
		// System.out.println(System.getProperty("os_name"));
		String[] p = { "a", "b", "c", "d" };
		System.out
				.println(SysFunc.getInstance().format(
						"%4@2s %3@2s %2@2s %1@2s",
						new String[] { "a", "b", "c", "d" }));
		// String a = "%4@2s %3@2s %2@2s %1@2s";
		// System.out.println(a.replace("@", "$"));
	}
}

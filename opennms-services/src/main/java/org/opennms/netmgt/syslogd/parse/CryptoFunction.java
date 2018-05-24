package org.opennms.netmgt.syslogd.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import twitter4j.http.BASE64Encoder;

public class CryptoFunction {

	public static final String ALGORITHM = "AES";

	public static final String ALGORITHM_FOR_CIPHER = "AES/CBC/PKCS5Padding";

	// init vector for randomness
	private static final byte[] iv = { (byte) 0xcb, (byte) 0x53, (byte) 0x03,
			(byte) 0x0f, (byte) 0xe0, (byte) 0x79, (byte) 0x9d, (byte) 0xdc,
			(byte) 0x80, (byte) 0xa9, (byte) 0x83, (byte) 0xf1, (byte) 0x03,
			(byte) 0xb6, (byte) 0x59, (byte) 0x83 };

	/**
	 * ��ԭʼ��ݽ��м���
	 * 
	 * @param plainText
	 *            ԭʼ���
	 * @param password
	 *            ����
	 * @return ���ܵ����
	 */
	public static String encrypt(String plainText, String password) {
		try {
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			SecretKey key = new SecretKeySpec(genKeyFromPassword(password),
					ALGORITHM);
			Cipher cipher = Cipher.getInstance(ALGORITHM_FOR_CIPHER);
			cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
			byte[] cipherText = cipher.doFinal(plainText.getBytes("UTF-8"));
			return base64Encode(cipherText);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * �Լ��ܵ���ݽ��н���
	 * 
	 * @param cipherText
	 *            ���ܵ����
	 * @param password
	 *            ����
	 * @return �������
	 */
	public static String decrypt(String cipherText, String password) {
		try {
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			SecretKey key = new SecretKeySpec(genKeyFromPassword(password),
					ALGORITHM);
			Cipher cipher = Cipher.getInstance(ALGORITHM_FOR_CIPHER);
			cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
			byte[] plainText = cipher.doFinal(base64Decode(cipherText));
			return new String(plainText, "UTF-8");
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * ����Base64����
	 * 
	 * @param rawbytes
	 * @return
	 */
	private static String base64Encode(byte[] rawbytes) {
		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(rawbytes);
	}

	/**
	 * ����Base64����
	 * 
	 * @param rawbytes
	 * @return byte[]
	 */
	private static byte[] base64Decode(String rawbyte)
			throws java.io.IOException {
		/*BASE64Decoder decoder = new BASE64Decoder();
		return decoder.decodeBuffer(rawbyte);*/
		return null;
	}

	/**
	 * ͨ�����������Կ
	 * 
	 * @param s
	 * @return
	 */
	private static byte[] genKeyFromPassword(String s) {
		return md5sum(s.toString().getBytes());
	}

	/**
	 * ����md5����Ҫ����ͳһ���볤��
	 * 
	 * @param buffer
	 * @return
	 */
	private static byte[] md5sum(byte[] buffer) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(buffer);
			return md5.digest();
		} catch (NoSuchAlgorithmException e) {
		}
		return null;
	}

	public static boolean encryptFile(File sfile, String password) {
		try {
			char[] buffer = new char[1024];
			InputStreamReader sfilereader = new InputStreamReader(
					new FileInputStream(sfile), "UTF-8");
			StringWriter swriter = new StringWriter();
			while (true) {
				int re = sfilereader.read(buffer);
				if (re == -1)
					break;
				swriter.write(buffer, 0, re);
			}
			String str = swriter.toString();
			if (CryptoFunction.decrypt(str, password) != null)
				return true;
			String dstring = CryptoFunction.encrypt(str, password);
			if (dstring == null)
				return false;
			swriter.close();
			sfilereader.close();
			OutputStreamWriter dfilewriter = new OutputStreamWriter(
					new FileOutputStream(sfile), "UTF-8");
			dfilewriter.write(dstring);
			dfilewriter.close();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static boolean decryptFile(File sfile, String password) {
		try {
			char[] buffer = new char[1024];
			InputStreamReader sfilereader = new InputStreamReader(
					new FileInputStream(sfile), "UTF-8");
			StringWriter swriter = new StringWriter();
			while (true) {
				int re = sfilereader.read(buffer);
				if (re == -1)
					break;
				swriter.write(buffer, 0, re);
			}
			String dstring = CryptoFunction.decrypt(swriter.toString(),
					password);
			swriter.close();
			sfilereader.close();
			if (dstring == null)
				return false;
			OutputStreamWriter dfilewriter = new OutputStreamWriter(
					new FileOutputStream(sfile), "UTF-8");
			dfilewriter.write(dstring);
			dfilewriter.close();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static byte[] decryptFileToString(File sfile, String password) {
		byte[] restr = null;
		try {
			char[] buffer = new char[1024];
			InputStreamReader sfilereader = new InputStreamReader(
					new FileInputStream(sfile), "UTF-8");
			StringWriter swriter = new StringWriter();
			while (true) {
				int re = sfilereader.read(buffer);
				if (re == -1)
					break;
				swriter.write(buffer, 0, re);
			}
			String dstr = CryptoFunction.decrypt(swriter.toString(), password);
			if (dstr != null)
				restr = CryptoFunction.decrypt(swriter.toString(), password)
						.getBytes("UTF-8");
			sfilereader.close();
			swriter.close();
		} catch (Exception e) {
			return restr;
		}
		return restr;
	}

	public static void main(String[] args) throws Exception {
		long cur = System.currentTimeMillis();
		// File sfile=new File("ext/audit/event/secgate.xml");

		// File dfile=new File("ext/audit/event/secgate_d.xml");
		// encryptFile(efile,"secfoxaudit");
		// boolean re=encryptFile(efile,"secfoxaudit");
		// decryptFile(efile,"secfoxaudit");

		// /**
		// * 1�����ļ����ܲ����Ϊ�µ�xml�ļ�
		// */
		String strNeedDecodeFile = "C:\\Users\\admin\\Desktop\\Firewall_CiscoPIX_FW.xml";
		String decodepath = "C:\\Users\\admin\\Desktop\\";
		File efile = new File(strNeedDecodeFile);
		XmlManager manager = new XmlManager();
		if (efile.isDirectory()) {
			String[] tmp = efile.list();
			for (int i = 0; i < tmp.length; i++) {
				File f = new File(strNeedDecodeFile + File.separator + tmp[i]);
				if (f.isFile() && f.getName().endsWith(".xml")) {
					try {
						manager.readEncryptFile(f, "secfoxaudit");
					} catch (Exception e) {
						e.printStackTrace();
					}
					System.out.print(manager.getRootElement().getName() + ":");
					manager.write(new File(decodepath + File.separator + tmp[i]));
					System.out.println("Encode file:" + efile.getName());
				}
			}

		} else {
			try {
				manager.readEncryptFile(efile, "secfoxaudit");
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.print(manager.getRootElement().getName() + ":");
			manager.write(new File(strNeedDecodeFile.substring(0,
					strNeedDecodeFile.length() - 4) + "_Decode.xml"));
			System.out.println(System.currentTimeMillis() - cur);
		}
		// /**
		// * 2�����ļ����ܲ����Ϊ�µ�xml�ļ�
		// */
		// String path = "C:\\Users\\admin\\Desktop\\Server_Windows.xml";
		// File file = new File(path);
		// if (file.isFile()) {
		// File efile = new File(path);
		// if (efile.isFile() && efile.getName().endsWith(".xml")) {
		// encryptFile(efile, "secfoxaudit");
		// System.out.println("Encode file:" + efile.getName());
		// }
		// } else {
		// String[] tmpFiles = file.list();
		// int i = 0;
		// for (String tmpFile : tmpFiles) {
		// File efile = new File(path + tmpFile);
		// if (efile.isFile() && efile.getName().endsWith(".xml")) {
		// encryptFile(efile, "secfoxaudit");
		// System.out.println("Encode file:" + tmpFile);
		// i++;
		// }
		// }
		// System.out.println("-------------------");
		// System.out.println("�������ļ���" + i + "��");
		// }
		//
		// String[] queryString = { "select
		// net_device.Name,t_vulnfoundvalue.CalTime,SUM(t_vulnfoundvalue.CalRecords)
		// vulnfound___cnt from t_vulnfoundvalue inner join net_device on
		// t_vulnfoundvalue.DeviceID=net_device.ID where
		// t_vulnfoundvalue.CalTime>=1323852761045 and
		// t_vulnfoundvalue.CalTime<1324457561045 and t_vulnfoundvalue.DeviceID
		// in (1029,1031) group by net_device.Name,t_vulnfoundvalue.CalTime
		// order by net_device.Name,t_vulnfoundvalue.CalTime" };
		//
		// Connection conn = DBUtil.getConnection();
		// PreparedStatement ps = conn.prepareStatement(queryString[0],
		// ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
		// ResultSet rs = ps.executeQuery();
		// // dataSource[i] = new DataSourceDefault(conn,rs);
		// ResultSetMetaData rsMeta = rs.getMetaData();
		//
		// int columnNum = rsMeta.getColumnCount();
		//
		// for (int j = 1; j <= columnNum; j++) {
		//
		// System.out.println("tableName=" + rsMeta.getTableName(j));
		// System.out.println("columnName=" + rsMeta.getColumnName(j));
		// }

		/**
		 * 3������License.dat�ļ�
		 */
		// String lfile = "C:\\Documents and
		// Settings\\Administrator.LENOVO-9DF8CE87\\����\\license.dat";
		// Properties licensepro = new Properties();
		// try {
		// licensepro.loadFromXML(new FileInputStream(lfile));
		//
		// if (licensepro.getProperty("lt4") == null) {
		// System.out.println("License�ļ�����");
		//
		// }
		//
		// String cipherSerial = licensepro.getProperty("st1");
		// if ((cipherSerial == null) || (cipherSerial.equals(""))) {
		// System.out.println("License���ݴ���st1Ϊ��");
		// }
		//
		// String cipherFirstTime = licensepro.getProperty("lt1");
		// if ((cipherFirstTime == null) || (cipherFirstTime.equals(""))) {
		// System.out.println("License���ݴ���lt1Ϊ��");
		// }
		//
		// String cipherRunTime = licensepro.getProperty("lt2");
		// if ((cipherRunTime == null) || (cipherRunTime.equals(""))) {
		// System.out.println("License���ݴ���lt2Ϊ��");
		// }
		//
		// String cipherLicenseTime = licensepro.getProperty("lt3");
		// if ((cipherLicenseTime == null) || (cipherLicenseTime.equals(""))) {
		// System.out.println("License���ݴ���lt3Ϊ��");
		// }
		//
		// String cipherCurrentKey = licensepro.getProperty("lt4");
		// if ((cipherCurrentKey == null) || (cipherCurrentKey.equals(""))) {
		// System.out.println("License���ݴ���lt4Ϊ��");
		// }
		//
		// String curkey = CryptoFunction.decrypt(cipherCurrentKey,
		// "legendsec");
		//
		// String firsttime = CryptoFunction.decrypt(cipherFirstTime, curkey);
		// String runtime = CryptoFunction.decrypt(cipherRunTime, curkey);
		// String ltime = CryptoFunction.decrypt(cipherLicenseTime, curkey);
		//
		// String serialstr = CryptoFunction.decrypt(cipherSerial, curkey);
		//
		// System.out.println("ϵͳ��һ������ʱ��firsttime=" + firsttime);
		// System.out.println("runtime=" + runtime);
		// System.out.println("License��Чʱ��ltime=" + Long.parseLong(ltime)
		// / 86400000L + "��");
		// System.out.println("serialstr=" + serialstr);
		//
		// String[] serialstrs = serialstr.split(":");
		//
		// if (serialstrs.length == 4) {
		// int licenseNum = Integer.parseInt(serialstrs[1]);
		// String module = serialstrs[2];
		// String userInfo = serialstrs[3];
		// System.out.println("licenseNum=" + licenseNum);
		// System.out.println("module=" + module);
		// System.out.println("userInfo=" + userInfo);
		// } else {
		// System.out.println("License���ݴ���st1���ܺ󳤶Ȳ���4λ");
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		//
		/**
		 * 4����ʾϵͳĬ�ϱ��뼰����
		 */
		// System.out.println(System.getProperty("file.encoding"));
		// System.out.println(System.getProperty("user.language"));
		// System.out.println(System.getProperty("user.region"));
		//
		// String str3 = System.getProperty("SIMSyslogParse");
		// System.out.println("str3=" + str3);
		/**
		 * 5������layout-config.xml�ļ�
		 */
		// String configFile = "C:\\Documents and
		// Settings\\Administrator.LENOVO-9DF8CE87\\����\\���ݹ�����\\���ζ����޸�\\layout-config.xml";
		// String default_encoding = "UTF-8";
		// SAXBuilder builder = new SAXBuilder();
		// Document doc = null;
		// InputStream is = null;
		// ByteArrayInputStream bais = null;
		// Reader reader = null;
		// File file = new File(configFile);
		// DESCipherUtil cipher = new DESCipherUtil(true);
		// try {
		// is = new FileInputStream(file);
		//
		// byte[] bytes = new byte[(int) file.length()];
		// for (int ii = 0; ii < bytes.length; ii++)
		// bytes[ii] = (byte) is.read();
		// byte[] realBytes = cipher.decrypt(bytes);
		// bais = new ByteArrayInputStream(realBytes);
		//
		// reader = new InputStreamReader(bais, default_encoding);
		// doc = builder.build(reader, default_encoding);
		//
		// XMLOutputter domstream = new XMLOutputter();
		// Format format = domstream.getFormat();
		// format.setEncoding("gb2312");
		// domstream.setFormat(format);
		// java.io.FileWriter writer = new java.io.FileWriter(configFile
		// .substring(0, configFile.length() - 4)
		// + "_Decode.xml");
		// domstream.output(doc, writer);
		// writer.close();
		//
		// } catch (Exception e) {// UnsupportedEncodingException,JDOMException,
		// // IOException, FileNotFoundException
		// e.printStackTrace();
		// System.out.println("Layout config init failed!");
		// return;
		// }
		/**
		 * 6��������ݿ�����
		 */
		// Connection conn = null;
		// try {
		// Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver")
		// .newInstance();
		//
		// conn = DriverManager
		// .getConnection(
		// "jdbc:microsoft:sqlserver://10.70.23.99:1433;DatabaseName=DB_CustomSMS",
		// "sa", "sa");
		//
		// System.out.println("OK");
		// } catch (SQLException e) {
		// e.printStackTrace();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		/**
		 * 7������WebService�����в���
		 */
		// String urlString =
		// "http://www.webxml.com.cn/webservices/qqOnlineWebService.asmx";//
		// ��Ϊ�ṩ��webservice��ַ
		// String xmlFile = "C:\\Documents and
		// Settings\\Administrator.LENOVO-9DF8CE87\\����\\QQOnlineService.XML";//
		// ����Է���xml�ĵ�����webservice˵���У��鿴soap���Ͳ��ּ���
		// String soapActionString = "http://WebXml.com.cn/qqCheckOnline";//
		// ��Ϊ���õķ���qqCheckOnline������ռ�
		//
		// URL url = new URL(urlString);
		//
		// HttpURLConnection httpConn = (HttpURLConnection)
		// url.openConnection();
		// ;
		//
		// File fileToSend = new File(xmlFile);
		// byte[] buf = new byte[(int) fileToSend.length()];
		// new FileInputStream(xmlFile).read(buf);
		// httpConn.setRequestProperty("Content-Length", String
		// .valueOf(buf.length));
		// httpConn.setRequestProperty("Content-Type",
		// "text/xml;charset=utf-8");
		// httpConn.setRequestProperty("User-Agent",
		// "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
		// httpConn.setRequestProperty("soapActionString", soapActionString);
		// httpConn.setRequestMethod("POST");
		// httpConn.setDoOutput(true);
		// httpConn.setDoInput(true);
		// OutputStream out = httpConn.getOutputStream();
		// out.write(buf);
		// out.close();
		//
		// InputStreamReader isr = new InputStreamReader(
		// httpConn.getInputStream(), "utf-8");
		// BufferedReader in = new BufferedReader(isr);
		//
		// String inputLine;
		// BufferedWriter bw = new BufferedWriter(
		// new OutputStreamWriter(
		// new FileOutputStream(
		// "C:\\Documents and
		// Settings\\Administrator.LENOVO-9DF8CE87\\����\\result.xml")));//
		// ������ɵ�xml�ĵ�
		// while ((inputLine = in.readLine()) != null) {
		// System.out.println(inputLine);
		// bw.write(inputLine);
		// bw.newLine();
		// }
		// bw.close();
		// in.close();
		/**
		 * 8�����˿��Ƿ�ռ��
		 */
		// boolean isBind = false;
		// int port = 1037;
		// try {
		// bindPort("0.0.0.0", port);
		// bindPort(InetAddress.getLocalHost().getHostAddress(), port);
		//
		// } catch (Exception e) {
		// isBind = true;
		// }
		//
		// System.out.println(port + " port is binded " + isBind);
		// �����ô���
		// String ss1 = CryptoFunction.decrypt("Ead5HnLX+SMysBzyY6u1tw==",
		// "legendsec");
		// System.out.println("******ss1=" + ss1 + "******");
		// String ss2 =
		// CryptoFunction.encrypt("00-16-EC-A4-3C-58:2:none,SecFoxSNI,SecFoxLAS:�������ຼ����Ϣ��������",
		// "legendsec");
		// CryptoFunction.encrypt("04-7D-7B-15-5B-7E:5:none,SecFoxSNI,SecFoxLAS:�������ຼ����Ϣ��������",
		// "legendsec");
		// CryptoFunction.encrypt("00-50-56-A6-11-02:5:none,SecFoxSNI,SecFoxLAS:�������ຼ����Ϣ��������",
		// "legendsec");
		// String ss2 =
		// CryptoFunction.encrypt("00-15-17-53-DA-DC:1000:none,SecFoxSNI,SecFoxLAS:�������ຼ����Ϣ��22������",
		// "legendsec");
		// System.out.println("******ss2=" + ss2 + "******");
		// UDPFlushService udp = new UDPFlushService();
		// System.out.println("���¼���sourceMap...");
		// udp.reloadSourceMap();
		// System.out.println("����SourceMap���");
		System.out.println("------------");

	}
}

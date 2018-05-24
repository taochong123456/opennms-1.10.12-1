package org.opennms.core.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class Util {
	private static String htmlMailTpl = "";
	public static final String IPV4_REGEX = "\\A(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}\\z";
	public static final String IPV6_HEX4DECCOMPRESSED_REGEX = "\\A((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}:)*)(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}\\z";
	public static final String IPV6_6HEX4DEC_REGEX = "\\A((?:[0-9A-Fa-f]{1,4}:){6,6})(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}\\z";
	public static final String IPV6_HEXCOMPRESSED_REGEX = "\\A((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)\\z";
	public static final String IPV6_REGEX = "\\A(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}\\z";
	public static Pattern ipv4_pattern = Pattern
			.compile("\\A(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}\\z");
	public static Pattern ipv6_HxDc_compressed_pattern = Pattern
			.compile("\\A((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}:)*)(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}\\z");
	public static Pattern ipv6_HxDc_pattern = Pattern
			.compile("\\A((?:[0-9A-Fa-f]{1,4}:){6,6})(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}\\z");
	public static Pattern ipv6_Hx_compressed_pattern = Pattern
			.compile("\\A((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)\\z");
	public static Pattern ipv6_pattern = Pattern
			.compile("\\A(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}\\z");

	public static final DecimalFormat SIZE_DECIMAL_FORMAT = new DecimalFormat(
			"0.00", new DecimalFormatSymbols(Locale.US));

	private static final char[] HEXSTRING = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static boolean isWindows() {
		String osName = System.getProperty("os.name");
		boolean isWindows = (osName.startsWith("Windows"))
				|| (osName.startsWith("windows"));
		return isWindows;
	}

	public static String findReplace(String str, String find, String replace) {
		StringBuilder replacedString = new StringBuilder();
		while (str.indexOf(find) != -1) {
			replacedString.append(str.substring(0, str.indexOf(find)));
			replacedString.append(replace);
			str = str.substring(str.indexOf(find) + find.length());
		}
		replacedString.append(str);
		return replacedString.toString();
	}

	public static void logStackTrace(Logger logger, Throwable e) {
		if (e == null) {
			return;
		}
		StackTraceElement[] ste = e.getStackTrace();
		logger.log(
				Level.SEVERE,
				new StringBuilder().append(" Exception Message : ")
						.append(e.getMessage()).append("\n").toString());
		StringBuilder sb = new StringBuilder();
		for (StackTraceElement st : ste) {
			sb.append(new StringBuilder().append("\t")
					.append(st.getClassName()).append(".")
					.append(st.getFileName()).append(" (")
					.append(st.getMethodName()).append(":")
					.append(st.getLineNumber()).append(")\n").toString());
		}
		logger.log(Level.SEVERE, sb.toString());
	}

	public static ArrayList<String> unZipFile(String unzipFolderPath,
			String zipFileName) {
		ArrayList fileNames = new ArrayList();
		BufferedOutputStream bufferOutput = null;
		FileInputStream fileInput = null;
		FileOutputStream fileOutput = null;
		ZipInputStream zipInput = null;
		try {
			fileInput = new FileInputStream(zipFileName);
			zipInput = new ZipInputStream(new BufferedInputStream(fileInput));
			ZipEntry entry;
			while ((entry = zipInput.getNextEntry()) != null) {
				String fileName = entry.getName();
				if (fileName.lastIndexOf("\\") != -1) {
					fileName = fileName
							.substring(fileName.lastIndexOf("\\") + 1);
				} else if (fileName.lastIndexOf("/") != -1) {
					fileName = fileName
							.substring(fileName.lastIndexOf("/") + 1);
				}

				byte[] data = new byte[1000];
				try {
					File f = new File(new StringBuilder()
							.append(unzipFolderPath).append(fileName)
							.toString());
					if (f.exists()) {
						f.delete();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				fileOutput = new FileOutputStream(new StringBuilder()
						.append(unzipFolderPath).append(fileName).toString());
				bufferOutput = new BufferedOutputStream(fileOutput, 1000);
				int count;
				while ((count = zipInput.read(data, 0, 1000)) != -1) {
					bufferOutput.write(data, 0, count);
				}
				bufferOutput.flush();
				bufferOutput.close();
				fileOutput.flush();
				fileOutput.close();
				bufferOutput = null;
				fileOutput = null;
				fileNames.add(fileName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bufferOutput != null) {
				try {
					bufferOutput.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (fileInput != null) {
				try {
					fileInput.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (zipInput != null) {
				try {
					zipInput.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (fileOutput != null) {
				try {
					fileOutput.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		deleteFile(
				new StringBuilder().append(unzipFolderPath).append(zipFileName)
						.toString(), Logger.getLogger("URL"));
		return fileNames;
	}

	public static boolean createZipFile(ArrayList<String> fileNames,
			String zipFileName) {
		boolean zipCreationStatus = false;
		int buffer = 2048;
		BufferedInputStream bufferInput = null;
		FileOutputStream fileOutput = null;
		ZipOutputStream zipOutput = null;
		FileInputStream fileInput = null;
		try {
			if (fileNames.size() > 0) {
				fileOutput = new FileOutputStream(zipFileName);
				zipOutput = new ZipOutputStream(new BufferedOutputStream(
						fileOutput));

				byte[] data = new byte[buffer];
				for (int i = 0; i < fileNames.size(); i++) {
					String fileNameWithPath = (String) fileNames.get(i);
					fileInput = new FileInputStream(fileNameWithPath);
					bufferInput = new BufferedInputStream(fileInput, buffer);
					String fileName = fileNameWithPath;
					if (fileNameWithPath.lastIndexOf("\\") != -1) {
						fileName = fileNameWithPath.substring(fileNameWithPath
								.lastIndexOf("\\") + 1);
					} else if (fileNameWithPath.lastIndexOf("/") != -1) {
						fileName = fileNameWithPath.substring(fileNameWithPath
								.lastIndexOf("/") + 1);
					}
					ZipEntry entry = new ZipEntry(fileName);
					zipOutput.putNextEntry(entry);
					int count;
					while ((count = bufferInput.read(data, 0, buffer)) != -1) {
						zipOutput.write(data, 0, count);
					}
					bufferInput.close();
					bufferInput = null;
					fileInput.close();
					fileInput = null;
				}
				zipOutput.close();
				zipOutput = null;
				zipCreationStatus = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bufferInput != null) {
				try {
					bufferInput.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (fileOutput != null) {
				try {
					fileOutput.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (fileInput != null) {
				try {
					fileInput.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (zipOutput != null) {
				try {
					zipOutput.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return zipCreationStatus;
	}

	public static String getMD5String(String response) {
		String md5String = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(response.getBytes());
			byte[] digest = md.digest();
			StringBuffer sbf = new StringBuffer();

			for (byte b : digest) {
				sbf.append(Integer.toHexString(b & 0xFF));
			}
			md5String = sbf.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return trim(md5String);
	}

	public static double getSizeInKB(long bytes) {
		double kBytes = bytes / 1024.0D;
		kBytes = new Double(SIZE_DECIMAL_FORMAT.format(kBytes)).doubleValue();
		return kBytes;
	}

	public static double getSizeInMB(long bytes) {
		double megaBytes = bytes / 1048576.0D;
		megaBytes = new Double(SIZE_DECIMAL_FORMAT.format(megaBytes))
				.doubleValue();
		return megaBytes;
	}

	public static String escapeXML(String s) {
		StringBuilder str = new StringBuilder();
		int len = s != null ? s.length() : 0;
		for (int i = 0; i < len; i++) {
			char ch = s.charAt(i);
			switch (ch) {
			case '<':
				str.append("&lt;");
				break;
			case '>':
				str.append("&gt;");
				break;
			case '&':
				str.append("&amp;");
				break;
			case '"':
				str.append("&quot;");
				break;
			case '\'':
				str.append("&apos;");
				break;
			default:
				str.append(ch);
			}
		}
		return str.toString();
	}

	public static String getProperty(Hashtable config, String key,
			String defaultVal) {
		try {
			if (config.containsKey(key)) {
				String val = new StringBuilder().append("")
						.append(config.get(key)).toString();
				if ((val != null) && (!val.equals("null")) && (!val.equals(""))) {
					return val;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defaultVal;
	}

	public static String getConfigNotInSyncErrorResponse() {
		StringBuilder sb = new StringBuilder(
				"<?xml version=\"1.0\" standalone=\"no\"?>");
		sb.append("\n<DC error_code=\"").append(String.valueOf(3001))
				.append("\"");
		sb.append(" availability=\"").append(String.valueOf(0)).append("\"");
		sb.append(" error_reason=\"").append("Config.data not in sync")
				.append("\"");
		sb.append(">");
		sb.append("\n</DC>");
		return sb.toString();
	}

	public static String getStackTrace(Throwable aThrowable) {
		return htmlMailTpl;
	}

	public static long parseLong(String value, long defaultvalue) {
		try {
			value = value.trim();
			long val = Long.parseLong(value);
			return val;
		} catch (Exception e) {
		}
		return defaultvalue;
	}

	public static String trim(String str) {
		return trim(str, "");
	}

	public static String trim(String str, String defaultvalue) {
		try {
			str = str.trim();
		} catch (Exception e) {
			str = defaultvalue;
		}
		return str;
	}

	public static String format(long time) {
		String format = "";
		time /= 1000L;
		long totalMins = time / 60L;
		long avaSecs = time % 60L;
		long totalHours = totalMins / 60L;
		long avaMins = totalMins % 60L;

		format = new StringBuilder().append(avaMins).append(" Mins ")
				.toString();
		if (totalHours == 0L) {
			format = new StringBuilder().append(format).append(avaSecs)
					.append(" Secs").toString();
		} else {
			if (avaSecs > 30L) {
				avaMins += 1L;
			}
			if (avaMins == 60L) {
				totalHours += 1L;
				avaMins = 0L;
			}
			long totalDays = totalHours / 24L;
			long avaHours = totalHours % 24L;
			if (totalDays == 0L) {
				format = new StringBuilder().append(totalHours).append(" Hrs ")
						.append(avaMins).append(" Mins ").toString();
			} else {
				format = new StringBuilder().append(totalDays).append(" days ")
						.append(avaHours).append(" Hrs ").append(avaMins)
						.append(" Mins ").toString();
			}
		}
		return format;
	}

	public static int parseInt(String value, int defaultvalue) {
		try {
			value = value.trim();
			int val = Integer.parseInt(value);
			return val;
		} catch (Exception e) {
		}
		return defaultvalue;
	}

	public static boolean getBooleanValue(String value, boolean defaultvalue) {
		try {
			value = trim(value);
			if (value.equals("true")) {
				return true;
			}
			if (value.equals("false")) {
				return false;
			}
		} catch (Exception e) {
			return defaultvalue;
		}
		return defaultvalue;
	}

	public static Hashtable replaceValues(Hashtable temp, String key) {
		if (temp.containsKey(key)) {
			String val = new StringBuilder().append("").append(temp.get(key))
					.toString();
			if ((val.equalsIgnoreCase("Yes")) || (val.equalsIgnoreCase("true"))) {
				temp.put(key, "true");
			} else {
				temp.put(key, "false");
			}
		}
		return temp;
	}

	public static Properties getFileAsProperties(String path) {
		Properties prop = new Properties();
		try {
			File f = new File(path);
			if (!f.exists()) {
				return null;
			}

			FileInputStream fis = null;
			try {
				fis = new FileInputStream(f);
				prop.load(fis);
			} finally {
				if (fis != null) {
					fis.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return prop;
	}

	public static boolean isValidFile(File file) {
		if (!file.exists()) {
			return false;
		}
		if (!file.isFile()) {
			return false;
		}

		if (!file.canRead()) {
			return false;
		}

		if (!file.canWrite()) {
			return false;
		}
		return true;
	}

	public static void replaceParamsInFile(String f, String[][] valueChangeArr) {
		try {
			File afile = new File(f);
			if (isValidFile(afile))
				try {
					BufferedReader bufferedreader = new BufferedReader(
							new FileReader(afile));
					StringBuffer buffer = new StringBuffer();
					String s2;
					while ((s2 = bufferedreader.readLine()) != null) {
						String finalval = null;
						for (int m = 0; m < valueChangeArr.length; m++) {
							String pattern = valueChangeArr[m][0];
							String tobeReplaced = valueChangeArr[m][1];

							while (s2.indexOf(pattern) != -1) {
								s2 = new StringBuilder()
										.append(s2.substring(0,
												s2.indexOf(pattern)))
										.append(tobeReplaced)
										.append(s2.substring(
												s2.indexOf(pattern)
														+ pattern.length(),
												s2.length())).toString();
								finalval = s2;
							}
						}

						if (finalval == null) {
							finalval = s2;
						}
						buffer.append(finalval);
						buffer.append("\n");
					}
					bufferedreader.close();
					PrintWriter pw = new PrintWriter(new BufferedWriter(
							new FileWriter(afile)));
					pw.println(buffer.toString());
					pw.flush();
					pw.close();
				} catch (Exception exception) {
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean updatePropertyFile(String file, Properties prop) {
		return false;
	}

	public static void deleteFile(String fileName, Logger logger) {
		try {
			File f = new File(fileName);
			if (f.exists()) {
				f.delete();
			}
		} catch (Exception e) {
			logger.log(Level.INFO,
					new StringBuilder().append("Exception while delete File(")
							.append(fileName).append(") : ").append(e)
							.toString());
		}
	}

	public static boolean doFileCopy(String src, String dest) {
		return false;
	}

	public static boolean checkPort(int port) {
		try {
			ServerSocket socket = new ServerSocket(port);
			socket.close();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static String getApmErrKey(int errorCode) {
		return new StringBuilder().append("eum.dc.errorcode.")
				.append(errorCode).toString();
	}

	public static void main(String[] args) {
	}

	public static boolean isIPV6Address(String address) {
		try {
			InetAddress inetAddr = InetAddress.getByName(address);
			return inetAddr instanceof Inet6Address;
		} catch (Exception exp) {
		}
		return false;
	}

	public static boolean isIPV4Address(String address) {
		try {
			InetAddress inetAddr = InetAddress.getByName(address);
			return inetAddr instanceof Inet4Address;
		} catch (Exception exp) {
		}
		return false;
	}

	public static boolean isLinkLocalAddress(String address) {
		try {
			InetAddress inetAddr = InetAddress.getByName(address);
			if ((inetAddr instanceof Inet6Address)) {
				return inetAddr.isLinkLocalAddress();
			}
		} catch (Exception e) {
		}
		return false;
	}

	public static String getLiteralAddress(String address) {
		if (!isIPV6Address(address)) {
			return address;
		}

		String literalAddress = address;
		if (isValidAddressFormat(address)) {
			literalAddress = new StringBuilder().append("[").append(address)
					.append("]").toString();
		}
		return literalAddress;
	}

	public static boolean isValidAddressFormat(String address) {
		boolean isZoneIDExists = address.lastIndexOf("%") != -1;
		String ipAddress = isZoneIDExists ? address.substring(0,
				address.lastIndexOf("%")) : address;

		if (ipv4_pattern.matcher(ipAddress).find()) {
			return true;
		}

		if (ipv6_HxDc_compressed_pattern.matcher(ipAddress).find()) {
			return true;
		}

		if (ipv6_HxDc_pattern.matcher(ipAddress).find()) {
			return true;
		}

		if (ipv6_Hx_compressed_pattern.matcher(ipAddress).find()) {
			return true;
		}

		return ipv6_pattern.matcher(ipAddress).find();
	}

	public static String getIPV6Address(String address) throws Exception {
		return address;
	}

	public static String getNetworkInterfaceName() {
		String intfName = null;
		try {
			Enumeration enu = NetworkInterface.getNetworkInterfaces();
			while (enu.hasMoreElements()) {
				NetworkInterface intfCard = (NetworkInterface) enu
						.nextElement();
				String name = intfCard.getName();
				Enumeration enu1 = intfCard.getInetAddresses();
				if (enu1.hasMoreElements()) {
					InetAddress ipObj = (InetAddress) enu1.nextElement();
					if ((ipObj instanceof Inet6Address)) {
						if (ipObj.isLoopbackAddress()) {
							continue;
						}
						if (intfName == null) {
							intfName = name;
							break;
						}
					}
				}
			}
		} catch (Exception ex) {
		}
		return intfName;
	}

	public static String getIpv6AddressAsString(byte[] ipv6addr) {
		if (ipv6addr.length != 16) {
			return null;
		}
		StringBuilder addrBuf = new StringBuilder();

		for (int i = 0; i < 16;) {
			if (ipv6addr[i] == 0) {
				getHexString(addrBuf, ipv6addr[(i + 1)], true);
			} else {
				getHexString(addrBuf, ipv6addr[i], true);
				getHexString(addrBuf, ipv6addr[(i + 1)], false);
			}
			addrBuf.append(":");
			i += 2;
		}
		addrBuf.setCharAt(addrBuf.length() - 1, ' ');
		return addrBuf.toString().trim();
	}

	public static byte[] getNetMask(int addressPrefixLength) {
		int numberOfBytesForIpv6Address = 16;
		int numberOfBitsPerByte = 8;
		if (addressPrefixLength > 128) {
			return null;
		}

		byte[] netmask = new byte[numberOfBytesForIpv6Address];

		int numberOfBytes = addressPrefixLength / numberOfBitsPerByte;
		for (int i = 0; i < numberOfBytes; i++) {
			netmask[i] = -1;
		}

		int remainingBits = addressPrefixLength - numberOfBytes
				* numberOfBitsPerByte;
		if (remainingBits > 0) {
			int nextByteVal = 255 << 8 - remainingBits;
			netmask[numberOfBytes] = (byte) nextByteVal;
		}
		return netmask;
	}

	public static void getHexString(StringBuilder addrBuf, byte hexval,
			boolean skipLeadingZero) {
		int firstHex = (hexval & 0xFF) >> 4;
		if ((firstHex == 0) && (skipLeadingZero)) {
			addrBuf.append(HEXSTRING[(hexval & 0xF)]);
		} else {
			addrBuf.append(HEXSTRING[firstHex]);
			addrBuf.append(HEXSTRING[(hexval & 0xF)]);
		}
	}

	public static String removeUnwantedChars(String sourceString) {
		StringBuffer strBuffer = new StringBuffer();
		if ((sourceString == null) || (sourceString.trim().equals(""))) {
			return "";
		}
		char[] charArray = sourceString.toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			if (Character.isIdentifierIgnorable(charArray[i])) {
				continue;
			}
			strBuffer.append(charArray[i]);
		}
		return strBuffer.toString();
	}

	public static String getFormattedIPV6Address(String hostName) {
		StringBuffer sb1 = new StringBuffer("[");
		sb1.append(hostName);
		sb1.append("]");
		hostName = sb1.toString();
		return hostName;
	}

	public static String getDynamicContent() {
		return new StringBuilder().append(System.currentTimeMillis())
				.append("_").append(genRanWd(6)).toString();
	}

	public static String genRanWd(int length) {
		Random random = new Random();
		String ran = "ABCDEFGHJKMNPQRSTUVWXYZabdefghjkmnpqrstuvwxyz123456789";
		int count = 0;
		String ranWord = "";
		while (count < length) {
			int i = random.nextInt();
			Integer val = new Integer(i);
			val = new Integer(Math.abs(val.intValue()));
			int dummy = val.intValue() % 54;
			ranWord = new StringBuilder().append(ranWord)
					.append(ran.charAt(dummy)).toString();
			count++;
		}
		return ranWord;
	}

	public static String getURI(String url) {
		String[] tokens = url.split("/");
		StringBuffer uri = new StringBuffer();
		int i = 0;
		for (i = 0; i < tokens.length; i++) {
			if (i < 3) {
				continue;
			}
			uri.append("/");
			uri.append(tokens[i]);
		}
		if (i < 3) {
			uri = new StringBuffer("/");
		}
		if (url.endsWith("/")) {
			uri.append("/");
		}
		return uri.toString();
	}

	public static String getThroughputValue(Long responseTime,
			Long contentLength) {
		String throughtputValue = "0";
		try {
			BigDecimal contentlength = new BigDecimal(contentLength.longValue());
			BigDecimal rt = new BigDecimal(responseTime.longValue());
			BigDecimal timeInSec = rt.divide(new BigDecimal(1000), 4, 0);
			BigDecimal throughput = BigDecimal.ZERO;
			if (timeInSec.compareTo(throughput) > 0) {
				throughput = contentlength.divide(timeInSec, 4, 0);
			}
			BigDecimal throughputKB = throughput.divide(new BigDecimal(1024),
					4, 0);
			DecimalFormat df = new DecimalFormat("#.##");
			throughtputValue = df.format(throughputKB.doubleValue());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return throughtputValue;
	}

	public static void setServerAgentTimeDiff(String servertime, String tzID) {
	}

	public static String readFile(String f) {
		return f;
	}

	public static String getServerDownXml(String mid, String locid) {
		StringBuilder sb = new StringBuilder(
				"<?xml version=\"1.0\" standalone=\"no\"?><APP>");
		try {
			sb.append("\n<DC ");
			sb.append(" mid=\"").append(String.valueOf(locid)).append("\"");
			sb.append(" locid=\"").append(String.valueOf(locid)).append("\"");
			sb.append(" availability=\"").append(String.valueOf(0))
					.append("\"");
			sb.append(" responsetime=\"").append(String.valueOf("0"))
					.append("\"");
			sb.append(" error_code=\"").append(String.valueOf(8000))
					.append("\"");
			sb.append(" ct=\"")
					.append(String.valueOf(System.currentTimeMillis()))
					.append("\"");
			sb.append(" reason=\"").append(String.valueOf("Server Down"))
					.append("\"");
			sb.append(">\n");
			sb.append("</DC></APP>");
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	public static void addRowElement(Document doc, Element row, String text) {
		Element value = doc.createElement("Value");
		Text col = doc.createTextNode(text);
		value.appendChild(col);
		row.appendChild(value);
	}

	public static void addRowElement(Document doc, Element row, String[] values) {
		for (String text : values)
			addRowElement(doc, row, text);
	}

	public static String getReponseInXml(Document doc) {
		StringWriter sw = new StringWriter();
		try {
			String isProbe = System.getProperty("agent.probe", "false");
			if ((isProbe != null) && (isProbe.equalsIgnoreCase("true"))) {
				formatTabularData(doc);
			}
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty("indent", "yes");
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sw.toString();
	}

	public static HashMap<String, String> getReponseInMap(Document doc,
			HashMap<String, String> map) {
		map.put("xmlResponse", getReponseInXml(doc));
		return map;
	}

	public static void setErrorMsg(Element rootNode, String scriptAvailability,
			String responseTime, String scriptMsg, String customeErrmsg,
			String errorCode) {
		rootNode.setAttribute("script_availability", scriptAvailability);
		rootNode.setAttribute("script_responsetime", responseTime);
		rootNode.setAttribute("script_message", scriptMsg);
		rootNode.setAttribute("custom_dc_error_message", customeErrmsg);
		rootNode.setAttribute("custom_dc_error_code", errorCode);
	}

	public static String convertVectorToString(String firstElement,
			Vector inVector) {
		String tempString = new String();
		if (firstElement != null) {
			tempString = new StringBuilder().append(firstElement).append(",")
					.toString();
		}
		for (int i = 0; i < inVector.size(); i++) {
			String temp1 = (String) inVector.elementAt(i);
			tempString = new StringBuilder().append(tempString).append(temp1)
					.append(",").toString();
		}
		if (tempString.indexOf(",") != -1) {
			tempString = tempString.substring(0, tempString.lastIndexOf(","));
		}
		return tempString;
	}

	public static void formatTabularData(Document doc) {
		formatTabularData(doc, null);
	}

	public static void formatTabularData(Document doc, Hashtable props) {
	}

	public static boolean isValidZipFile(File file) {
		return false;
	}

	public static double getDiffDoubleValue(double currentValue, double oldValue) {
		double diffValue = currentValue - oldValue < 0.0D ? 0.0D : currentValue
				- oldValue;
		return diffValue;
	}

	public static long getDiffLongValue(long currentValue, long oldValue) {
		long diffValue = currentValue - oldValue < 0L ? 0L : currentValue
				- oldValue;
		return diffValue;
	}

	public static double fetchDoubleValueFromRootNode(Element rootElement,
			String attributeName) {
		double value = -1.0D;
		try {
			value = Double
					.parseDouble(new StringBuilder().append("")
							.append(rootElement.getAttribute(attributeName))
							.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}

	public static Long fetchLongValueFromRootNode(Element rootElement,
			String attributeName) {
		long value = -1L;
		try {
			value = Long
					.parseLong(new StringBuilder().append("")
							.append(rootElement.getAttribute(attributeName))
							.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Long.valueOf(value);
	}

	public static String getBuildNo(String agentVersion) {
		String buildNo = agentVersion.replace(".", "");
		return buildNo;
	}

	public static Hashtable storeFileInDFS(String userid, String fileName,
			String content) {
		try {
			Class dfsUtil = Class.forName("com.adventnet.webmon.util.DFSUtil");
			Class[] paramTypes = { String.class, String.class, String.class };
			Method meth = dfsUtil.getDeclaredMethod("storeFileInDFS",
					paramTypes);

			Object[] params = { userid, fileName, content };
			Object dfsDetails = meth.invoke(dfsUtil, params);
			if (dfsDetails != null) {
				return (Hashtable) dfsDetails;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Hashtable storeFileInDFS(String userid, File f) {
		try {
			Class dfsUtil = Class.forName("com.adventnet.webmon.util.DFSUtil");
			Class[] paramTypes = { String.class, File.class };
			Method meth = dfsUtil.getDeclaredMethod("storeFileInDFS",
					paramTypes);

			Object[] params = { userid, f };
			Object dfsDetails = meth.invoke(dfsUtil, params);
			if (dfsDetails != null) {
				return (Hashtable) dfsDetails;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Properties getIPvTypes(String resolvedIps) {
		Properties ipTypes = new Properties();
		ArrayList ip4 = new ArrayList();
		ArrayList ip6 = new ArrayList();

		if ((resolvedIps != null) && (!resolvedIps.equals(""))) {
			String[] ips = resolvedIps.split(",");
			for (int i = 0; i < ips.length; i++) {
				if (isIPV6Address(ips[i])) {
					ip6.add(ips[i]);
				} else {
					if (!isIPV4Address(ips[i]))
						continue;
					ip4.add(ips[i]);
				}
			}
		}
		ipTypes.put("ip4", ip4);
		ipTypes.put("ip6", ip6);
		return ipTypes;
	}

	public static boolean upStatusCodeCheck(Integer statusCode,
			String expression) {
		boolean availability = Boolean.FALSE.booleanValue();
		if (expression.trim().length() > 2) {
			String[] expressionList = expression.split(",");
			for (String exp : expressionList) {
				if (exp.contains(":")) {
					String[] range = exp.split(":");
					if ((statusCode.compareTo(Integer.valueOf(Integer
							.parseInt(range[0]))) >= 0)
							&& (statusCode.compareTo(Integer.valueOf(Integer
									.parseInt(range[1]))) <= 0)) {
						availability = Boolean.TRUE.booleanValue();
						break;
					}
				} else {
					if ((!Character.isDigit(exp.charAt(0)))
							|| (exp.contains(":"))
							|| (!statusCode.equals(Integer.valueOf(Integer
									.parseInt(exp)))))
						continue;
					availability = Boolean.TRUE.booleanValue();
					break;
				}
			}
		}
		return availability;
	}
}
package org.opennms.netmgt.syslogd.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Element;

public class DefaultEventFormater implements EventFormater {
	private ArrayList<EventPattern> eventMatchs = new ArrayList<EventPattern>();
	private String configDir = System.getProperty("opennms.home")
			+ "/etc/parser/";
	private Calendar calendar = Calendar.getInstance();
	private HashMap fieldMap = new HashMap();
	private HashMap matchMap = new HashMap();
	private ArrayList<EventPattern> splitList = new ArrayList<EventPattern>();
	private Properties eventMap = new Properties();//

	private class SortComparator implements Comparator {
		public SortComparator() {
		}

		public int compare(Object a, Object b) {
			EventPattern ap = (EventPattern) a;
			EventPattern bp = (EventPattern) b;
			if (ap.getPriority() < bp.getPriority())
				return -1;
			else if (ap.getPriority() == bp.getPriority())
				return 0;
			else
				return 1;
		}
	}

	public DefaultEventFormater() {
		init(null);
	}

	public DefaultEventFormater(File confFile) {
		init(confFile);
	}

	/**
	 * �����豸
	 * 
	 * @param devMap
	 */
	public void initDeviceMap(Map devMap) {

	}

	/**
	 * ��ʼ���¼�����
	 * 
	 */
	private void initEventFormatType() {
		eventMap.clear();
		String sfile = configDir + "status.properties";
		try {
			InputStreamReader sfilereader = new InputStreamReader(
					new FileInputStream(sfile), "UTF-8");
			eventMap.load(sfilereader);
			sfilereader.close();
		} catch (Exception e) {
		}

	}

	/**
	 * ��ȡ�����ļ�����ʼ���¼���һ���� ����°��¼����Խ����޸� liyue 2012-9-7
	 */
	@SuppressWarnings("unchecked")
	private void init(File confFile) {
		// ��ʼ����һ���ֶζ�Ӧֵ
		fieldMap.put("category", 0);
		fieldMap.put("type", 1);
		fieldMap.put("priority", 2);
		// fieldMap.put("oriPriority",3); //�°汾�¼�������ȡ���ֶ�
		fieldMap.put("rawID", 3);
		fieldMap.put("occurTime", 4);
		// fieldMap.put("sendTime",6); //�°汾�¼�������ȡ���ֶ�
		fieldMap.put("duration", 5);
		fieldMap.put("send", 6);
		fieldMap.put("receive", 7);
		fieldMap.put("protocol", 8);
		fieldMap.put("appProtocol", 9);
		fieldMap.put("object", 10);
		fieldMap.put("policy", 11);
		fieldMap.put("resource", 12);
		fieldMap.put("action", 13);
		fieldMap.put("intent", 14);
		fieldMap.put("result", 15);
		fieldMap.put("sAddr", 16);
		// fieldMap.put("sName",19); //�°汾�¼�������ȡ���ֶ�
		fieldMap.put("sPort", 17);
		// fieldMap.put("sProcess",21); //�°汾�¼�������ȡ���ֶ�
		// fieldMap.put("sUserID",22); //�°汾�¼�������ȡ���ֶ�
		fieldMap.put("sUserName", 18);
		fieldMap.put("stAddr", 19);
		fieldMap.put("stPort", 20);
		fieldMap.put("dAddr", 21);
		// fieldMap.put("dName",27); //�°汾�¼�������ȡ���ֶ�
		fieldMap.put("dPort", 22);
		// fieldMap.put("dProcess",29); //�°汾�¼�������ȡ���ֶ�
		// fieldMap.put("dUserID",30); //�°汾�¼�������ȡ���ֶ�
		fieldMap.put("dUserName", 23);
		fieldMap.put("dtAddr", 24);
		fieldMap.put("dtPort", 25);
		fieldMap.put("devAddr", 26);
		fieldMap.put("devName", 27);
		fieldMap.put("devCategory", 28);
		fieldMap.put("devType", 29);
		fieldMap.put("devVendor", 30);
		fieldMap.put("programType", 31);
		// fieldMap.put("program",40); //�°汾�¼�������ȡ���ֶ�
		fieldMap.put("requestURI", 32);
		fieldMap.put("name", 33);
		fieldMap.put("customS1", 34);
		fieldMap.put("customS2", 35);
		fieldMap.put("customS3", 36);
		fieldMap.put("customS4", 37);
		fieldMap.put("customD1", 38);
		fieldMap.put("customD2", 39);
		fieldMap.put("devProduct", 40);
		fieldMap.put("sessionID", 41);
		// fieldMap.put("customS5",51); //�°汾�¼�������ȡ���ֶ�
		// fieldMap.put("customS6",52); //�°汾�¼������Ѹı�ΪsMAC
		fieldMap.put("sMAC", 42);
		// fieldMap.put("customS7",53); //�°汾�¼������Ѹı�ΪdMAC
		fieldMap.put("dMAC", 43);
		fieldMap.put("customD3", 44);
		fieldMap.put("customD4", 45);
		fieldMap.put("collectType", 46);
		// fieldMap.put("sensorAddr",57); //�°汾�¼�������ȡ���ֶ�
		// fieldMap.put("sensorName",58); //�°汾�¼�������ȡ���ֶ�

		if (confFile == null) {
			initEventFormatType();

			File dir = new File(configDir);
			File[] confFiles = dir.listFiles();
			for (int i = 0; i < confFiles.length; i++) {

				if (confFiles[i].isFile()
						&& confFiles[i].getName().toLowerCase()
								.endsWith(".xml")) {
					String namestr = confFiles[i].getName().substring(0,
							confFiles[i].getName().length() - 4);
					String typestr = this.eventMap.getProperty(namestr);
					// if (typestr != null &&
					// typestr.equalsIgnoreCase("enabled"))
					if ("ADS_Blackhole".equals(namestr)) {
						readConfFile(confFiles[i]);
					}
				}
			}
		} else {
			readConfFile(confFile);
		}
		// ���ý�������
		Collections.sort(eventMatchs, new SortComparator());
	}

	/**
	 * ��ȡÿ�������ļ�
	 * 
	 * @param confFile
	 */
	private void readConfFile(File confFile) {
		try {
			XmlManager xmlmanager = new XmlManager();
			readConfFile(confFile);
			/*
			 * xmlmanager.readEncryptFile(confFile, "secfoxaudit");
			 */Iterator ievents = xmlmanager.getRootElement().getChildren()
					.iterator();
			while (ievents.hasNext()) {
				Element event = (Element) ievents.next();
				EventPattern eventPattern = new EventPattern();
				eventPattern.setName(event.getAttributeValue("Name"));
				String matchstr = event.getAttributeValue("Match");
				if (!matchstr.equals("")) {
					Pattern p = Pattern.compile(matchstr);
					eventPattern.setPattern(p);
					eventMatchs.add(eventPattern);
					String splitstr = event.getAttributeValue("Split");
					if (splitstr != null) {
						eventPattern.setSplitMatch(splitstr);
						splitList.add(eventPattern);
					}
				}

				String prioritystr = event.getAttributeValue("Priority");
				if (!prioritystr.equals("")) {
					eventPattern.setPriority(Integer.parseInt(prioritystr));
				}

				String nodetype = event.getAttributeValue("NodeType");
				if (nodetype != null) {
					eventPattern.setNodeType(nodetype);
				}

				String encode = event.getAttributeValue("Encode");
				if (encode != null) {
					eventPattern.setEncode(encode);
				}

				List fPatternList = event.getChildren();
				EventFieldPattern[] fPatterns = new EventFieldPattern[fieldMap
						.size()];
				eventPattern.setFieldPattern(fPatterns);
				for (int i = 0; i < fPatternList.size(); i++) {
					Element field = (Element) fPatternList.get(i);
					EventFieldPattern fPattern = new EventFieldPattern();
					fPattern.setName(field.getAttributeValue("Name"));
					String fmatchstr = field.getAttributeValue("Index");
					if (!fmatchstr.equals("")) {
						String[] indexstrs = fmatchstr.split("\\+");
						int[] indexs = new int[indexstrs.length];
						for (int j = 0; j < indexstrs.length; j++) {
							indexs[j] = Integer.parseInt(indexstrs[j]);
						}
						fPattern.setMatchIndex(indexs);
					}

					String fformatstr = field.getAttributeValue("Format");
					if (!fformatstr.equals("")) {
						if (fformatstr.indexOf("yy") == -1) {
							fPattern.setBaseTime(EventFieldPattern.BASETIME_YEAR);
							fformatstr = fformatstr + " yyyy";
						}
						if (fformatstr.indexOf("MMM") > -1
								|| fformatstr.indexOf("EEE") > -1) {
							SimpleDateFormat fFormat = new SimpleDateFormat(
									fformatstr, Locale.US);
							fPattern.setFormat(fFormat);
						} else {
							SimpleDateFormat fFormat = new SimpleDateFormat(
									fformatstr);
							fPattern.setFormat(fFormat);
						}
					}

					List fieldvalues = field.getChildren();
					if (fieldvalues != null && fieldvalues.size() > 0) {
						Object[] matches = new Object[fieldvalues.size()];
						HashMap keyValue = new HashMap();
						for (int k = 0; k < fieldvalues.size(); k++) {
							Element fieldvalue = (Element) fieldvalues.get(k);
							String key = fieldvalue.getAttributeValue("Key");
							if (key != null) {
								keyValue.put(key,
										fieldvalue.getAttributeValue("Value"));
								continue;
							}

							String[] mapvalue = new String[2];
							mapvalue[0] = fieldvalue.getAttributeValue("Match");
							mapvalue[1] = fieldvalue.getAttributeValue("Value");
							matches[k] = mapvalue;
						}

						if (keyValue.isEmpty())
							fPattern.setValue(matches);
						else
							fPattern.setValue(keyValue);
					}

					// added by zhuzhen 20110526
					String fExp = field.getAttributeValue("Exp");
					if (fExp != null && !fExp.equals("")) {
						fPattern.setExp(EventFieldExpression.createExp(fExp));
					}

					String fvaluestr = field.getAttributeValue("Value");
					if (!fvaluestr.equals("")) {
						fPattern.setDefaultValue(fvaluestr);
					}

					Integer index = (Integer) fieldMap.get(fPattern.getName());
					if (index != null) {
						if (fPattern.getMatchIndex() == null
								&& fPattern.getValue() == null
								&& fPattern.getDefaultValue() == null
								&& fPattern.getExp() == null)// addeb
																// by
																// zhuzhen
																// fPattern.getExp()==null
							fPatterns[index.intValue()] = null;
						else
							fPatterns[index.intValue()] = fPattern;
					}
				}
			}
		} catch (Exception e) {
		}
	}

	/**
	 * ����һ���µ��¼�����
	 * 
	 * @param event
	 * @return
	 */
	private SIMEventObject cloneEventObject(SIMEventObject event) {
		SIMEventObject logOb = new SIMEventObject();
		logOb.ID = 0;
		if (logOb.priority == null)
			logOb.priority = 1;
		logOb.occurTime = event.occurTime;
		// logOb.sendTime=event.sendTime; //�°汾�¼�������ȡ���ֶ�
		logOb.devAddr = event.devAddr;
		logOb.sysType = event.sysType;
		logOb.collectType = event.collectType;
		logOb.collectorAddr = event.collectorAddr;
		logOb.collectorName = event.collectorName;
		return logOb;
	}

	/**
	 * ��һ���¼���
	 * ��һ�������SIMEventObject��msg�ֶ��н�������ֶΣ���SIMEventObject���и�ֵ��
	 * 
	 * @param event
	 * @return
	 */
	public SIMEventObject[] format(SIMEventObject event) {

		// ��ʼ��
		if (event.priority == null)
			event.priority = 1;

		// ����ƥ���豸��ַ
		ArrayList matchlist = (ArrayList) this.matchMap.get(event.devAddr);
		if (matchlist != null) {
			for (int i = 0; i < matchlist.size(); i++) {
				EventPattern ep = (EventPattern) matchlist.get(i);
				Object reob = formatPattern(ep, event);
				if (reob != null) {
					if (reob instanceof SIMEventObject[])
						return (SIMEventObject[]) reob;
					else
						return null;
				}
			}
		}

		for (int i = 0; i < eventMatchs.size(); i++) {
			EventPattern ep = eventMatchs.get(i);
			Object reob = formatPattern(ep, event);
			if (reob != null) {
				if (reob instanceof SIMEventObject[])
					return (SIMEventObject[]) reob;
				else if (reob instanceof SIMEventObject) {
					return new SIMEventObject[] { (SIMEventObject) reob };
				}
			}
		}
		return null;
	}

	/**
	 * �����¼�ģʽ
	 * 
	 * @param ep
	 * @param event
	 * @return
	 */
	private Object formatPattern(EventPattern ep, SIMEventObject event) {
		if (ep.getSplitMatch() != null) {
			String[] eventstrs = new String[60]; // added by zhuzhen
													// 20121208,��Ҫ�Ǳ�֤�̰߳�ȫ��60�Ǽٶ�һ����ݰ���������Ϣ�в��ᳬ��60��
			int start = 0;
			int size = 0;
			while (start < event.msg.length()) {
				int end = event.msg.indexOf(ep.getSplitMatch(), start);
				if (end == -1 && start == 0)
					return null;
				else if (end == -1)
					end = event.msg.length();
				eventstrs[size] = event.msg.substring(start, end);
				start = end + ep.getSplitMatch().length();
				size++;
			}

			boolean isfind = false;
			SIMEventObject[] newEvents = new SIMEventObject[size];
			for (int k = 0; k < size; k++) {
				newEvents[k] = cloneEventObject(event);
				newEvents[k].msg = eventstrs[k];
				for (int j = 0; j < splitList.size(); j++) {
					EventPattern sep = splitList.get(j);
					Pattern p = sep.getPattern();
					String msg = newEvents[k].msg;
					if (sep.getEncode() != null) {
						try {
							msg = new String(newEvents[k].msg.getBytes(),
									ep.getEncode());
						} catch (java.io.UnsupportedEncodingException e) {
						}
					}
					Matcher m = p.matcher(msg);
					if (m.find()) {
						// sep.setMatcher(m);// modify by liyue
						// Ϊ��ֹ�������ң����ٸ�ep��matcher��ֵ
						newEvents[k].msg = msg;
						parseEvent(sep, m, newEvents[k]);
						isfind = true;
						break;
					}
				}
			}

			if (isfind)
				return newEvents;
		} else {
			Pattern p = ep.getPattern();
			String msg = event.msg;
			if (ep.getEncode() != null) {
				try {
					msg = new String(event.msg.getBytes(), ep.getEncode());
				} catch (java.io.UnsupportedEncodingException e) {
				}
			}
			Matcher m = p.matcher(msg);
			try {
				if (m.find()) {
					System.out.println("===================================");
					event.msg = msg;
					parseEvent(ep, m, event);
					System.out
							.println("===================ssss================");

					return event;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private SIMEventObject parseEvent(EventPattern ep, SIMEventObject event) {
		EventFieldPattern[] fp = ep.getFieldPattern();
		String[] eventfield = new String[fp.length];// added by zhuzhen
													// ��֤eventfield �̰߳�ȫ
													// 20121208
		for (int j = 0; j < fp.length; j++) {
			eventfield[j] = null;
			if (fp[j] != null) {
				if (fp[j].getMatchIndex() != null) {
					if (fp[j].getMatchIndex()[0] == 0)
						eventfield[j] = event.devAddr;
					else
						eventfield[j] = ep.getMatcher().group(
								fp[j].getMatchIndex()[0]);
					for (int k = 1; k < fp[j].getMatchIndex().length; k++)
						eventfield[j] = eventfield[j]
								+ ep.getMatcher().group(
										fp[j].getMatchIndex()[k]);
					if (fp[j].getValue() != null)
						eventfield[j] = fp[j].getFormatValue(eventfield[j]);
				}
				// add exp ���� zhuzhen 20110527
				if (fp[j].getExp() != null && eventfield[j] == null) {
					Object expResult = fp[j].getExp().getValue(event,
							ep.getMatcher());
					eventfield[j] = expResult != null ? expResult.toString()
							: null;
					if (fp[j].getValue() != null)
						eventfield[j] = fp[j].getFormatValue(eventfield[j]);
				}
				if (fp[j].getDefaultValue() != null && eventfield[j] == null)
					eventfield[j] = fp[j].getFormatValue(null);
			}
		}

		if (eventfield[0] != null)
			// event.category=eventfield[0]; //�°汾�¼������Ѹı������ֶ�
			event.category = Integer.parseInt(eventfield[0]);
		if (eventfield[1] != null)
			// event.type=eventfield[1]; //�°汾�¼������Ѹı������ֶ�
			event.type = Integer.parseInt(eventfield[1]);
		if (eventfield[2] != null) {
			try {
				event.priority = Integer.parseInt(eventfield[2]);
			} catch (NumberFormatException e) {
			}
		} else {
		}
		if (eventfield[3] != null) {
			try {
				event.rawID = Long.parseLong(eventfield[3]);
			} catch (NumberFormatException e) {
				event.rawID = new Long(0);
			}
		}
		if (eventfield[4] != null) {
			try {
				if (fp[4].getFormat() != null) {
					if (fp[4].getBaseTime() > 0)
						eventfield[4] = eventfield[4] + " "
								+ calendar.get(Calendar.YEAR);
					SimpleDateFormat sdf = (SimpleDateFormat) fp[4].getFormat();
					synchronized (sdf) {
						event.occurTime = sdf.parse(eventfield[4]).getTime();
					}
				} else
					event.occurTime = Long.parseLong(eventfield[4]);
			} catch (NumberFormatException e) {
				event.occurTime = System.currentTimeMillis();
			} catch (ParseException e) {
				event.occurTime = System.currentTimeMillis();
			}
		}
		if (eventfield[5] != null) {
			try {
				event.duration = Long.parseLong(eventfield[5]);
			} catch (NumberFormatException e) {
				event.duration = new Long(0);
			}
		}
		if (eventfield[6] != null) {
			try {
				event.send = Long.parseLong(eventfield[6]);
			} catch (NumberFormatException e) {
				event.send = new Long(0);
			}
		}
		if (eventfield[7] != null) {
			try {
				event.receive = Long.parseLong(eventfield[7]);
			} catch (NumberFormatException e) {
				event.receive = new Long(0);
			}
		}
		if (eventfield[8] != null) {
			try {
				event.protocol = Integer.parseInt(eventfield[8]);
			} catch (NumberFormatException e) {
				event.protocol = new Integer(0);
			}
		}
		if (eventfield[9] != null) {
			try {
				event.appProtocol = Integer.parseInt(eventfield[9]);
			} catch (NumberFormatException e) {
				event.appProtocol = new Integer(0);
			}
		}
		if (eventfield[10] != null) {
			try {
				event.object = Integer.parseInt(eventfield[10]);
			} catch (NumberFormatException e) {
				event.object = new Integer(0);
			}
		}
		if (eventfield[11] != null) {
			try {
				event.policy = Integer.parseInt(eventfield[11]);
			} catch (NumberFormatException e) {
				event.policy = new Integer(0);
			}
		}
		if (eventfield[12] != null)
			event.resource = eventfield[12];
		if (eventfield[13] != null) {
			try {
				event.action = Integer.parseInt(eventfield[13]);
			} catch (NumberFormatException e) {
				event.action = new Integer(0);
			}
		}
		if (eventfield[14] != null)
			event.intent = eventfield[14];
		if (eventfield[15] != null) {
			try {
				event.result = Integer.parseInt(eventfield[15]);
			} catch (NumberFormatException e) {
				event.result = new Integer(0);
			}
		}
		if (eventfield[16] != null)
			event.sAddr = eventfield[16];
		if (eventfield[17] != null) {
			try {
				event.sPort = Integer.parseInt(eventfield[17]);
			} catch (NumberFormatException e) {
				event.sPort = new Integer(0);
			}
		}
		if (eventfield[18] != null)
			event.sUserName = eventfield[18];
		if (eventfield[19] != null)
			event.stAddr = eventfield[19];
		if (eventfield[20] != null) {
			try {
				event.stPort = Integer.parseInt(eventfield[20]);
			} catch (NumberFormatException e) {
				event.stPort = new Integer(0);
			}
		}
		if (eventfield[21] != null)
			event.dAddr = eventfield[21];
		/**
		 * dName���°���ȡ���ֶ�
		 */
		// if (eventfield[27]!=null)
		// event.dName=eventfield[27];
		if (eventfield[22] != null) {
			try {
				event.dPort = Integer.parseInt(eventfield[22]);
			} catch (NumberFormatException e) {
				event.dPort = new Integer(0);
			}
		}
		if (eventfield[23] != null)
			event.dUserName = eventfield[23];
		if (eventfield[24] != null)
			event.dtAddr = eventfield[24];
		if (eventfield[25] != null) {
			try {
				event.dtPort = Integer.parseInt(eventfield[25]);
			} catch (NumberFormatException e) {
				event.dtPort = new Integer(0);
			}
		}
		if (eventfield[26] != null)
			event.devAddr = eventfield[26];
		if (eventfield[27] != null)
			event.devName = eventfield[27];
		if (eventfield[28] != null) {
			try {
				event.devCategory = Integer.parseInt(eventfield[28]);
			} catch (NumberFormatException e) {
				event.devCategory = new Integer(0);
			}
		}
		if (eventfield[29] != null) {
			try {
				event.devType = Integer.parseInt(eventfield[29]);
			} catch (NumberFormatException e) {
				event.devType = new Integer(0);
			}
		}
		if (eventfield[30] != null)
			event.devVendor = eventfield[30];
		if (eventfield[31] != null)
			event.programType = eventfield[31];
		/**
		 * program���°���ȡ���ֶ�
		 */
		// if (eventfield[40]!=null)
		// event.program=eventfield[40];
		if (eventfield[32] != null)
			event.requestURI = eventfield[32];
		if (eventfield[33] != null)
			event.name = eventfield[33];
		if (eventfield[34] != null)
			event.customS1 = eventfield[34];
		if (eventfield[35] != null)
			event.customS2 = eventfield[35];
		if (eventfield[36] != null)
			event.customS3 = eventfield[36];
		if (eventfield[37] != null)
			event.customS4 = eventfield[37];
		if (eventfield[38] != null) {
			try {
				event.customD1 = Long.parseLong(eventfield[38]);
			} catch (NumberFormatException e) {
				event.customD1 = new Long(0);
			}
		}
		if (eventfield[39] != null) {
			try {
				event.customD2 = Long.parseLong(eventfield[39]);
			} catch (NumberFormatException e) {
				event.customD2 = new Long(0);
			}
		}
		if (eventfield[40] != null)
			event.devProduct = eventfield[40];
		if (eventfield[41] != null)
			event.sessionID = eventfield[41];
		if (eventfield[42] != null)
			event.sMAC = eventfield[42];
		if (eventfield[43] != null)
			event.dMAC = eventfield[43];
		if (eventfield[44] != null) {
			try {
				event.customD3 = Double.parseDouble(eventfield[44]);
			} catch (NumberFormatException e) {
				event.customD3 = new Double(0);
			}
		}
		if (eventfield[45] != null) {
			try {
				event.customD4 = Double.parseDouble(eventfield[45]);
			} catch (NumberFormatException e) {
				event.customD4 = new Double(0);
			}
		}
		if (eventfield[46] != null) {
			try {
				event.collectType = Integer.parseInt(eventfield[46]);
			} catch (NumberFormatException e) {
			}
		}
		return null;
	}

	/**
	 * Add by liyue 2012-06-12 �����¼��п��Խ������ֶ� �����������Matcher
	 * m���Խ�������¼����󣬲�����ʱ���������ҵ�����
	 * 
	 * @param fp
	 * @param event
	 * @return
	 */
	private SIMEventObject parseEvent(EventPattern ep, Matcher m,
			SIMEventObject event) {
		EventFieldPattern[] fp = ep.getFieldPattern();
		String[] eventfield = new String[fp.length];// added by zhuzhen
													// ��֤eventfield �̰߳�ȫ
													// 20121208
		for (int j = 0; j < fp.length; j++) {
			eventfield[j] = null;
			if (fp[j] != null) {
				if (fp[j].getMatchIndex() != null) {
					if (fp[j].getMatchIndex()[0] == 0)
						eventfield[j] = event.devAddr;
					else
						eventfield[j] = m.group(fp[j].getMatchIndex()[0]);
					for (int k = 1; k < fp[j].getMatchIndex().length; k++)
						eventfield[j] = eventfield[j]
								+ m.group(fp[j].getMatchIndex()[k]);
					if (fp[j].getValue() != null)
						eventfield[j] = fp[j].getFormatValue(eventfield[j]);
				}
				// add exp ���� zhuzhen 20110527
				if (fp[j].getExp() != null && eventfield[j] == null) {
					Object expResult = fp[j].getExp().getValue(event, m);
					eventfield[j] = expResult != null ? expResult.toString()
							: null;
					if (fp[j].getValue() != null)
						eventfield[j] = fp[j].getFormatValue(eventfield[j]);
				}
				if (fp[j].getDefaultValue() != null && eventfield[j] == null)
					eventfield[j] = fp[j].getFormatValue(null);
			}
		}

		if (eventfield[0] != null) {
			try {
				event.category = Integer.parseInt(eventfield[0]);
			} catch (NumberFormatException e) {
			}
		}

		if (eventfield[1] != null) {
			try {
				event.type = Integer.parseInt(eventfield[1]);
			} catch (NumberFormatException e) {
			}
		}

		if (eventfield[2] != null) {
			try {
				event.priority = Integer.parseInt(eventfield[2]);
			} catch (NumberFormatException e) {
			}
		} else {

		}
		if (eventfield[3] != null) {
			try {
				event.rawID = Long.parseLong(eventfield[3]);
			} catch (NumberFormatException e) {
				event.rawID = new Long(0);
			}
		}
		if (eventfield[4] != null) {
			try {
				if (fp[4].getFormat() != null) {
					if (fp[4].getBaseTime() > 0)
						eventfield[4] = eventfield[4] + " "
								+ calendar.get(Calendar.YEAR);
					SimpleDateFormat sdf = (SimpleDateFormat) fp[4].getFormat();
					synchronized (sdf) {
						event.occurTime = sdf.parse(eventfield[4]).getTime();
					}
				} else
					event.occurTime = Long.parseLong(eventfield[4]);
			} catch (NumberFormatException e) {
				event.occurTime = System.currentTimeMillis();
			} catch (ParseException e) {
				event.occurTime = System.currentTimeMillis();
			}
		}

		if (eventfield[5] != null) {
			try {
				event.duration = Long.parseLong(eventfield[5]);
			} catch (NumberFormatException e) {
				event.duration = new Long(0);
			}
		}
		if (eventfield[6] != null) {
			try {
				event.send = Long.parseLong(eventfield[6]);
			} catch (NumberFormatException e) {
				event.send = new Long(0);
			}
		} else {
			// Modify by liyue 2012-12-14 Ϊsend�ֶθ�Ĭ��ֵΪ0���Է�ֹͳ�Ʊ���ʱ���?
			event.send = new Long(0);
		}
		if (eventfield[7] != null) {
			try {
				event.receive = Long.parseLong(eventfield[7]);
			} catch (NumberFormatException e) {
				event.receive = new Long(0);
			}
		} else {
			// Modify by liyue 2012-12-14 Ϊrecevie�ֶθ�Ĭ��ֵΪ0���Է�ֹͳ�Ʊ���ʱ���?
			event.receive = new Long(0);
		}
		if (eventfield[8] != null) {
			try {
				event.protocol = Integer.parseInt(eventfield[8]);
			} catch (NumberFormatException e) {
				event.protocol = new Integer(0);
			}
		}
		if (eventfield[9] != null) {
			try {
				event.appProtocol = Integer.parseInt(eventfield[9]);
			} catch (NumberFormatException e) {
				event.appProtocol = new Integer(0);
			}
		}
		/**
		 * object���°�ı������ֶ�
		 */
		// if (eventfield[12]!=null)
		// event.object=eventfield[12];
		if (eventfield[10] != null) {
			try {
				event.object = Integer.parseInt(eventfield[10]);
			} catch (NumberFormatException e) {
				event.object = new Integer(0);
			}
		}
		/**
		 * policy���°�ı������ֶ�
		 */
		// if (eventfield[13]!=null)
		// event.policy=eventfield[13];
		if (eventfield[11] != null) {
			try {
				event.policy = Integer.parseInt(eventfield[11]);
			} catch (NumberFormatException e) {
				event.policy = new Integer(0);
			}
		}
		if (eventfield[12] != null)
			event.resource = eventfield[12].length() <= 255 ? eventfield[12]
					: eventfield[12].substring(0, 255);
		/**
		 * action���°�ı������ֶ�
		 */
		// if (eventfield[15]!=null)
		// event.action=eventfield[15];
		if (eventfield[13] != null) {
			try {
				event.action = Integer.parseInt(eventfield[13]);
			} catch (NumberFormatException e) {
				event.action = new Integer(0);
			}
		}
		if (eventfield[14] != null)
			event.intent = eventfield[14];
		/**
		 * result���°�ı������ֶ�
		 */
		// if (eventfield[17]!=null)
		// event.result=eventfield[17];
		if (eventfield[15] != null) {
			try {
				event.result = Integer.parseInt(eventfield[15]);
			} catch (NumberFormatException e) {
				event.result = new Integer(0);
			}
		}
		if (eventfield[16] != null)
			event.sAddr = eventfield[16];
		if (eventfield[17] != null) {
			try {
				event.sPort = Integer.parseInt(eventfield[17]);
			} catch (NumberFormatException e) {
				event.sPort = new Integer(0);
			}
		}
		if (eventfield[18] != null)
			event.sUserName = eventfield[18].length() <= 255 ? eventfield[18]
					: eventfield[18].substring(0, 255);
		if (eventfield[19] != null)
			event.stAddr = eventfield[19];
		if (eventfield[20] != null) {
			try {
				event.stPort = Integer.parseInt(eventfield[20]);
			} catch (NumberFormatException e) {
				event.stPort = new Integer(0);
			}
		}
		if (eventfield[21] != null)
			event.dAddr = eventfield[21];
		if (eventfield[22] != null) {
			try {
				event.dPort = Integer.parseInt(eventfield[22]);
			} catch (NumberFormatException e) {
				event.dPort = new Integer(0);
			}
		}
		if (eventfield[23] != null)
			event.dUserName = eventfield[23].length() <= 255 ? eventfield[23]
					: eventfield[23].substring(0, 255);
		if (eventfield[24] != null)
			event.dtAddr = eventfield[24];
		if (eventfield[25] != null) {
			try {
				event.dtPort = Integer.parseInt(eventfield[25]);
			} catch (NumberFormatException e) {
				event.dtPort = new Integer(0);
			}
		}
		if (eventfield[26] != null)
			event.devAddr = eventfield[26];
		if (eventfield[27] != null)
			event.devName = eventfield[27].length() <= 255 ? eventfield[27]
					: eventfield[27].substring(0, 255);
		if (eventfield[28] != null) {
			try {
				event.devCategory = Integer.parseInt(eventfield[28]);
			} catch (NumberFormatException e) {
				event.devCategory = new Integer(0);
			}
		}
		if (eventfield[29] != null) {
			try {
				event.devType = Integer.parseInt(eventfield[29]);
			} catch (NumberFormatException e) {
				event.devType = new Integer(0);
			}
		}
		if (eventfield[30] != null)
			event.devVendor = eventfield[30].length() <= 255 ? eventfield[30]
					: eventfield[30].substring(0, 255);
		if (eventfield[31] != null)
			event.programType = eventfield[31].length() <= 255 ? eventfield[31]
					: eventfield[31].substring(0, 255);
		if (eventfield[32] != null)
			event.requestURI = eventfield[32];
		if (eventfield[33] != null)
			event.name = eventfield[33].length() <= 255 ? eventfield[33]
					: eventfield[33].substring(0, 255);
		if (eventfield[34] != null)
			event.customS1 = eventfield[34].length() <= 255 ? eventfield[34]
					: eventfield[34].substring(0, 255);
		if (eventfield[35] != null)
			event.customS2 = eventfield[35].length() <= 255 ? eventfield[35]
					: eventfield[35].substring(0, 255);
		if (eventfield[36] != null)
			event.customS3 = eventfield[36].length() <= 255 ? eventfield[36]
					: eventfield[36].substring(0, 255);
		if (eventfield[37] != null)
			event.customS4 = eventfield[37].length() <= 255 ? eventfield[37]
					: eventfield[37].substring(0, 255);
		if (eventfield[38] != null) {
			try {
				event.customD1 = Long.parseLong(eventfield[38]);
			} catch (NumberFormatException e) {
				event.customD1 = new Long(0);
			}
		}
		if (eventfield[39] != null) {
			try {
				event.customD2 = Long.parseLong(eventfield[39]);
			} catch (NumberFormatException e) {
				event.customD2 = new Long(0);
			}
		}
		if (eventfield[40] != null)
			event.devProduct = eventfield[40].length() <= 255 ? eventfield[40]
					: eventfield[40].substring(0, 255);
		if (eventfield[41] != null)
			event.sessionID = eventfield[41].length() <= 255 ? eventfield[41]
					: eventfield[41].substring(0, 255);
		if (eventfield[42] != null)
			event.sMAC = eventfield[42].length() <= 255 ? eventfield[42]
					: eventfield[42].substring(0, 255);
		if (eventfield[43] != null)
			event.dMAC = eventfield[43].length() <= 255 ? eventfield[43]
					: eventfield[43].substring(0, 255);
		if (eventfield[44] != null) {
			try {
				event.customD3 = Double.parseDouble(eventfield[44]);
			} catch (NumberFormatException e) {
				event.customD3 = new Double(0);
			}
		}
		if (eventfield[45] != null) {
			try {
				event.customD4 = Double.parseDouble(eventfield[45]);
			} catch (NumberFormatException e) {
				event.customD4 = new Double(0);
			}
		}
		if (eventfield[46] != null) {
			try {
				event.collectType = Integer.parseInt(eventfield[46]);
			} catch (NumberFormatException e) {
			}
		}

		return event;
	}
}

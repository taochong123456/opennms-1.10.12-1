package org.opennms.netmgt.syslogd.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;

class VarBind {
	String name;// ��$�Ĳ�������
	String paraName;// ����$�Ĳ�������
	int type;
}

public class EventFieldExpression {
	private static final JexlEngine jexl = new JexlEngine();
	static {
		jexl.setCache(1024);
		jexl.setLenient(false);
		jexl.setSilent(false);
	}

	// ����������� ��ʾʵ��ֵ����ԭʼ��־�ı�
	public static final int CONTENT = 1;
	// ����������� ��ʾʵ��ֵ���Թ�����¼�����
	public static final int EVENT = 2;
	// ����������� ��ʾʵ��ֵ����ϵͳԤ����ı���
	public static final int SYSTEM = 3;

	// ����������� ��ʾʵ��ֵ����ϵͳ���������ṩ�ķ���
	public static final int PUBLIC = 5;
	private static SysFunc sysFunc = SysFunc.getInstance();//

	/**
	 * ������ʽ
	 */
	private Expression e;
	/**
	 * ֵ�Σ����л�����
	 */
	private JexlContext jc;
	/**
	 * ���ʽ���õ��Ĳ����
	 */
	private VarBind[] vars;

	public Expression getE() {
		return e;
	}

	public void setE(Expression e) {
		this.e = e;
	}

	public JexlContext getJc() {
		return jc;
	}

	public void setJc(JexlContext jc) {
		this.jc = jc;
	}

	public VarBind[] getVars() {
		return vars;
	}

	public void setVars(VarBind[] vars) {
		this.vars = vars;
	}

	/**
	 * ���ָ�������ʵ��ֵ
	 * 
	 * @param var
	 * @return
	 */
	private Object getVarValue(SIMEventObject event, Matcher matcher,
			VarBind var) {
		Object varValue = null;
		switch (var.type) {
		case CONTENT:
			int index = Integer.parseInt(var.paraName);
			varValue = matcher.group(index);
			break;
		case EVENT:
			varValue = FilterAssistant.getSIMEventObjectAttribute(event,
					var.paraName);
			break;
		case SYSTEM:
			varValue = System.getProperty(var.paraName);

			break;
		case PUBLIC:
			varValue = sysFunc;
			break;
		default:
			break;
		}
		return varValue;
	}

	/**
	 * ��ݵ�ǰ�¼�����ñ��ʽ��ֵ
	 * 
	 * @param event
	 * @return
	 */
	public Object getValue(SIMEventObject event, Matcher matcher) {
		try {
			// �����ֵ
			if (vars != null) {
				for (int i = 0; i < vars.length; i++) {
					VarBind var = vars[i];
					jc.set(var.name, getVarValue(event, matcher, var));
				}
			}
			return e.evaluate(jc);
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * ��ݱ��ʽ����EventFieldExpression����
	 * 
	 * @param src
	 * @return
	 */
	public static EventFieldExpression createExp(String src) {
		try {
			EventFieldExpression efe = new EventFieldExpression();
			Expression e = jexl.createExpression(src);
			JexlContext jc = new MapContext();
			efe.setE(e);
			efe.setJc(jc);
			List<VarBind> params = new ArrayList<VarBind>();// �����б?��������$��ͷ��$$��ͷ�ı�ʾ��ϵͳ����
			int length = src.length();
			for (int i = 0; i < length; i++) {
				Character c = src.charAt(i);
				if (c == '$') {// ��$���Ǳ����ı�־
					i++;// ����һ���ַ�
					if (i >= length)// �Ѿ���ĩβ
						break;
					Character next = src.charAt(i);
					// if (next == '$') {// ��������$$��Ϊϵͳ����
					// flag = SYSTEM;
					// i++;// ���Ƶ���һ���ַ�
					// if (i >= length)
					// break;
					// }
					int start = i;
					while (Character.isJavaIdentifierPart(next = src.charAt(i))) {
						i++;
						if (i >= length)
							break;
					}
					String vName = src.substring(start, i);
					VarBind vb = new VarBind();
					try {
						int vIndex = Integer.parseInt(vName); // �������� $1
						vb.type = CONTENT;
						vb.paraName = String.valueOf(vIndex);
					} catch (Exception e2) {
						// TODO: handle exception
						if (vName.equalsIgnoreCase("sys")) {
							vb.type = PUBLIC;
							vb.paraName = vName.toLowerCase();
						} else if (FilterAssistant.fieldNameIndexMap
								.containsKey(vName.toLowerCase())) {
							vb.type = EVENT;// ��������$devaddrs
							vb.paraName = vName.toLowerCase();
						} else if (SysFunc.SysVar.contains(vName)) {
							vb.type = SYSTEM;
							vb.paraName = vName;
						} else {
							continue;
						}
					}
					vb.name = "$" + vName;
					params.add(vb);
				}
			}
			VarBind[] vbs = new VarBind[params.size()];
			for (int i = 0; i < vbs.length; i++) {
				vbs[i] = params.get(i);
			}
			efe.setVars(vbs);
			return efe;
		} catch (Exception e) {
		}
		return null;

	}

}

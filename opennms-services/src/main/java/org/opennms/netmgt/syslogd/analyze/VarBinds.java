package org.opennms.netmgt.syslogd.analyze;
public class VarBinds implements java.io.Serializable{
	
	/**
	 * 变量类型。可以是"event","constant","filter"等等。
	 */
	private String type;
	/**
	 * 引用的对象
	 */
	private String ref; 
	/**
	 * 引用的对象的某成员属性
	 */
	private String attrname;
	/**
	 * 该变量对应的一组值。通常在type为'constants'时,values才有意义,他表示一组常量值
	 */
	private String[] values;
	
	/**
	 * VarBinds内部使用的值对象
	 */
	private Object internValueObject;
	
	/**
	 * 设置value的值.对于只有一个值的情况,使用此方法.
	 * @param value
	 */
	public void setValue(String value){
		if(values==null){
			values=new String[1];			
		}
		values[0]=value;
	}
	/**
	 * 取得value的值,对于只有一个值的情况,使用此方法.
	 * @return
	 */
	public String getValue(){
		if(values==null||values.length==0)
			return null;
		else
			return values[0];
	}
	
	public String getAttrname() {
		return attrname;
	}
	public void setAttrname(String attrname) {
		this.attrname = attrname;
	}
	public String getRef() {
		return ref;
	}
	public void setRef(String ref) {
		this.ref = ref;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String[] getValues() {
		return values;
	}
	public void setValues(String[] values) {
		this.values = values;
	}
	public Object getInternValueObject() {
		return internValueObject;
	}
	public void setInternValueObject(Object internValueObject) {
		this.internValueObject = internValueObject;
	}

}
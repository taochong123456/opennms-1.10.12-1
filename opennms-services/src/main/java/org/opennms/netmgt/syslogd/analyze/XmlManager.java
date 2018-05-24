package org.opennms.netmgt.syslogd.analyze;

import java.io.*;

import org.apache.log4j.Logger;
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;
public class XmlManager {
	
	private static Logger logger = Logger.getLogger(XmlManager.class);
	
	private Document document = null;

	org.xml.sax.XMLFilter xmlFilter = null;

	private String xmlEncoding = "UTF-8";
	
	public XmlManager(){
		//System.out.println(" creating.... >>>>>>>>>>> ");
		//logger.info(" creating.... >>>>>>>>>>> XmlManager");
	}

	/**
	 * 设置XML过滤器
	 * 
	 * @param xmlFilter
	 *            定义XML过滤器
	 */
	public void setXMLFilter(org.xml.sax.XMLFilter xmlFilter) {
		this.xmlFilter = xmlFilter;

	}

	/**
	 * 从输入流中读取XML
	 * 
	 * @param inputStream
	 *            输入流
	 * @return true or false.
	 */
	public boolean read(InputStream inputStream) throws JDOMException {
		//logger.info(" read.... >>>>>>>>>>> XmlManager");
		SAXBuilder builder = null;
		builder = new SAXBuilder();
		if (xmlFilter != null)
			builder.setXMLFilter(xmlFilter);

		builder.setExpandEntities(true);
		if (inputStream == null)
			return false;
		try {
			document = builder.build(inputStream);
		} catch (java.io.IOException e) {
			e.printStackTrace();
			logger.info("read xml error."+e);
			xmlFilter = null;
			return false;
		}

		return true;
	}

	/**
	 * 从文件中读取xml
	 * 
	 * @param strFile
	 *            xml文件名
	 * @return true or false.
	 */
	public boolean read(String strFile) throws JDOMException {
		//logger.info(" read.... >>>>>>>>>>> XmlManager");
		SAXBuilder builder = null;
		builder = new SAXBuilder();
		if (xmlFilter != null)
			builder.setXMLFilter(xmlFilter);
		builder.setExpandEntities(true);
		if (document != null)
			document = null;

		try {
			document = builder.build(new File(strFile));
		} catch (java.io.IOException e) {
			e.printStackTrace();
			logger.info("read xml error."+e);
			xmlFilter = null;
			return false;
		}

		return true;

	}

	/**
	 * 从文件中读取xml
	 * 
	 * @param file
	 *            xml文件对象
	 * @return true or false.
	 */
	public boolean read(File file) throws JDOMException {
		//logger.info(" read.... >>>>>>>>>>> XmlManager");
		SAXBuilder builder = null;
		builder = new SAXBuilder();
		if (xmlFilter != null)
			builder.setXMLFilter(xmlFilter);
		builder.setExpandEntities(true);
		if (document != null)
			document = null;

		try {
			document = builder.build(file);
		} catch (java.io.IOException e) {
			e.printStackTrace();
			logger.info("read xml error."+e);
			xmlFilter = null;
			return false;
		}

		return true;

	}

	/**
	 * 读取加密文件
	 * @param file
	 * @param password
	 * @return
	 * @throws JDOMException
	 */
	public boolean readEncryptFile(File file, String password) throws JDOMException
	{
		return true;
	}
	
	/**
	 * 从StringReader中读取xml ,即从字符串中读取xml内容。
	 * 
	 * @param reader
	 *            xml Reader
	 * @return true or false.
	 */
	public boolean read(StringReader reader) throws JDOMException {
		//logger.info(" read.... >>>>>>>>>>> XmlManager");
		SAXBuilder builder = null;
		builder = new SAXBuilder();
		if (xmlFilter != null)
			builder.setXMLFilter(xmlFilter);
		builder.setExpandEntities(true);
		if (document != null)
			document = null;

		try {
			document = builder.build(reader);
		} catch (java.io.IOException e) {
			e.printStackTrace();
			logger.info("read xml error."+e);
			xmlFilter = null;
			return false;
		}

		return true;

	}

	/**
	 * 写入xml到输出流
	 * 
	 * @param outputStream
	 *            xml输出流
	 */
	public void write(OutputStream outputStream) throws IOException {
		XMLOutputter outputter = new XMLOutputter();
		Format outfort = Format.getPrettyFormat();
		outfort.setEncoding(xmlEncoding);
		outputter.setFormat(outfort);
		if (document != null)
			outputter.output(document, outputStream);
		
		//System.out.println(">> write outputStream");
	}

	/**
	 * 写入XML到文件中
	 * 
	 * @param strFile
	 *            xml文件名
	 */
	public void write(String strFile) throws IOException {
		//FileWriter fileWriter = new FileWriter(new File(strFile));
		XMLOutputter outputter = new XMLOutputter();
		Format outfort = Format.getPrettyFormat();
		outfort.setEncoding(xmlEncoding);
		outputter.setFormat(outfort);
		FileOutputStream fos = new FileOutputStream(strFile);
		outputter.output(document,fos );
		fos.close();
		//System.out.println(":DD:"+fileWriter.getEncoding());
		//fileWriter.close();
		
		//System.out.println(">> write "+strFile);
	}

	/**
	 * 写入XML到文件中
	 * 
	 * @param file
	 *            xml文件对象
	 */
	public void write(File file) throws IOException {
		//FileWriter fileWriter = new FileWriter(file);
		XMLOutputter outputter = new XMLOutputter();
		Format outfort = Format.getPrettyFormat();
		outfort.setEncoding(xmlEncoding);
		outputter.setFormat(outfort);
		FileOutputStream fos = new FileOutputStream(file);
		outputter.output(document, fos);
		fos.close();
		//fileWriter.close();
		
		//System.out.println(">> write "+file.getName());
	}

	/**
	 * 写入XML到文件中
	 * 
	 * @param file
	 *            xml文件对象
	 */
	public void write(StringWriter writer) throws IOException {
		XMLOutputter outputter = new XMLOutputter();
		Format outfort = Format.getPrettyFormat();
		outfort.setEncoding(xmlEncoding);
		outputter.setFormat(outfort);
		outputter.output(document, writer);
		if (writer != null)
			writer.close();
		
		//System.out.println(">> write string");
	}

	/**
	 * 设置xml的编码方式
	 * 
	 * @param encoding
	 *            编码方式.
	 */
	public void setXmlEncoding(String encoding) {
		xmlEncoding = encoding;
	}

	/**
	 * 创建xml文档
	 * 
	 * @param strRoot
	 *            xml文档的根节点名称
	 */
	public void createDocument(String strRoot) {
		if (document != null)
			document = null;

		Element root = new Element(strRoot);
		document = new Document(root);
	}

	/**
	 * 获取xml文档的根节点
	 * 
	 * @return
	 */
	public Element getRootElement() {
		if (document != null) {
			return document.getRootElement();
		} else
			return null;
	}

	/**
	 * 设置xml文档的根节点
	 * 
	 * @param element
	 */
	public void setRootElement(Element element) {
		if (document != null) {
			document.setRootElement(element);
		}
	}

	/**
	 * 
	 * 目的：将字符串xml转换成rootelement对象
	 * 
	 * @param str
	 * @return Element 根节点
	 * @throws JDOMException 
	 */
	public static Element getElement(String str) throws JDOMException {
		XmlManager xmlmanager = new XmlManager();
		xmlmanager.setXmlEncoding("UTF-8");
		try {
			xmlmanager.read(new StringReader(str));
		} catch (JDOMException e) {
			logger.error("XmlManager.getElement() error:", e);
			throw e;
		}
		return xmlmanager.getRootElement();
	}
	
	public static void main(String[] args) {
		XmlManager xmloperator = new XmlManager();
		try {
			String xmlStr="<?xml version='1.0' ?><a></a>";
			//if (xmloperator.read(new StringReader(xmlStr))) {
			if (xmloperator.read("b.xml")) {
				xmloperator.getRootElement().setText("中");
				System.out.println(xmloperator.getRootElement().getText());
				StringWriter result = new StringWriter();
				xmloperator.write("ok.xml");
				//System.out.println(result);
			}
			
			/*
			PrintWriter out = new PrintWriter(new BufferedWriter(new
       				OutputStreamWriter(new FileOutputStream("yyy"),"UTF-8")));
			out.println("中古");
			out.close();
			*/
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}

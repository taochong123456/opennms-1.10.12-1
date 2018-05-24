package org.opennms.netmgt.syslogd.parse;

import java.util.regex.*;
import java.util.*;

public class EventPattern {
	private String encode = null;
	private EventFieldPattern[] fieldPattern = null;
	private Matcher matcher = null;
	private String name = null;
	private String nodeType = null;
	private Pattern pattern = null;
	private int priority = 10000;
	private String splitMatch = null;

	public String getEncode() {
		return encode;
	}

	public EventFieldPattern[] getFieldPattern() {
		return fieldPattern;
	}

	public Matcher getMatcher() {
		return matcher;
	}

	public String getName() {
		return name;
	}

	public String getNodeType() {
		return nodeType;
	}

	public Pattern getPattern() {
		return pattern;
	}

	public int getPriority() {
		return priority;
	}

	public String getSplitMatch() {
		return splitMatch;
	}

	public void setEncode(String encode) {
		this.encode = encode;
	}

	public void setFieldPattern(EventFieldPattern[] fieldPattern) {
		this.fieldPattern = fieldPattern;
	}

	public void setMatcher(Matcher matcher) {
		this.matcher = matcher;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public void setSplitMatch(String splitMatch) {
		this.splitMatch = splitMatch;
	}

	@Override
	public String toString() {
		return "EventPattern [name=" + name + ", nodeType=" + nodeType
				+ ", pattern=" + pattern + ", fieldPattern="
				+ Arrays.toString(fieldPattern) + ", matcher=" + matcher
				+ ", priority=" + priority + ", splitMatch=" + splitMatch
				+ ", encode=" + encode + "]";
	}

}

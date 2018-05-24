package org.opennms.netmgt.syslogd.analyze;

import java.util.*;

public class KeywordsDes {
	private static TreeMap keywords;

	/**
	 * create Map between keyword and token type
	 * 
	 */
	public static void createKeywordsMap() {
		if (keywords == null) {
			keywords = new TreeMap();
			// put your key words and token pairs here
		}

	}

	/**
	 * fetch tokenType of special key words
	 * 
	 * @param keyword
	 *            keyword (usually got from LexParser.sval)
	 * @return tokenType if return -1 then the word is not a keyword.
	 */
	public static int getTokenValue(String keyword) {
		if (keywords == null)
			createKeywordsMap();
		Integer tokenType = (Integer) keywords.get(keyword);
		return tokenType != null ? tokenType.intValue() : -1;
	}
}

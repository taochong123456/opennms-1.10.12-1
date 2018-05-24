package org.opennms.core.utils;

import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

public class JsonTransform {
	@SuppressWarnings("unchecked")
	public static Map<String, Object> stringToMap(String jsonStr) {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = null;
		try {
			map = mapper.readValue(jsonStr, Map.class);
		} catch (Exception e) {
			log().error("Read json to object error!");
		}
		return map;

	}
	protected static ThreadCategory log() {
		return ThreadCategory.getInstance(JsonTransform.class);
	}
}

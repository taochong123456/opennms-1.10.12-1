package org.opennms.netmgt.utils;

import java.util.Hashtable;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.collectd.HttpCollector;

public class APIDatacollectorUtil {
	public static String getHostPortURL(Properties props) {
		String hostName = (String) props.get("HostName");
		int port = Integer.parseInt((String) props.get("Port"));

		String protocol = "http";
		if ((props.get("ssl") != null)
				&& (props.get("ssl").toString().equalsIgnoreCase("true"))) {
			protocol = "https";
		}
		String hosturl = protocol + "://" + hostName + ":" + port;
		return hosturl;
	}

	public static int getIntfromJSON(JSONObject jsonObj, String key) {
		try {
			if ((jsonObj.has(key)) && (jsonObj.get(key) != null))
				return jsonObj.getInt(key);
		} catch (Exception e) {
			log().error("APIDatacollectorUtil getIntfromJSON error:" + e);
			return -1;
		}
		return -1;
	}

	public static float getFloatfromJSON(JSONObject jsonObj, String key) {
		try {
			if ((jsonObj.has(key)) && (jsonObj.get(key) != null)) {
				String floatValue = jsonObj.getString(key);
				return Float.parseFloat(floatValue);
			}
		} catch (Exception e) {
			log().error("APIDatacollectorUtil getFloatfromJSON error:" + e);
			return -1.0F;
		}
		return -1.0F;
	}

	public static long getLongfromJSON(JSONObject jsonObj, String key) {
		try {
			if ((jsonObj.has(key)) && (jsonObj.get(key) != null))
				return jsonObj.getLong(key);
		} catch (Exception e) {
			log().error("APIDatacollectorUtil getLongfromJSON error:" + e);
			return -1L;
		}
		return -1L;
	}

	public static double getDoublefromJSON(JSONObject jsonObj, String key) {
		try {
			if ((jsonObj.has(key)) && (jsonObj.get(key) != null))
				return jsonObj.getDouble(key);
		} catch (Exception e) {
			log().error("APIDatacollectorUtil getDoublefromJSON error:" + e);
			return -1.0D;
		}
		return -1.0D;
	}

	public static String getStringfromJSON(JSONObject jsonObj, String key) {
		try {
			if ((jsonObj.has(key)) && (jsonObj.get(key) != null))
				return jsonObj.getString(key);
		} catch (Exception e) {
			log().error("APIDatacollectorUtil getStringfromJSON error:" + e);
			return "";
		}
		return "";
	}

	public static JSONObject getJsonObjectfromJSON(JSONObject jsonObj,
			String key) {
		try {
			if ((jsonObj.has(key)) && (jsonObj.get(key) != null))
				return jsonObj.getJSONObject(key);
		} catch (Exception e) {
			log().error("APIDatacollectorUtil getJsonObjectfromJSON error:" + e);
			return new JSONObject();
		}
		return new JSONObject();
	}

	public static JSONArray getJsonArrayfromJSON(JSONObject jsonObj, String key)
			throws JSONException {
		try {
			if ((jsonObj.has(key)) && (jsonObj.get(key) != null))
				return jsonObj.getJSONArray(key);
		} catch (Exception e) {
			log().error("APIDatacollectorUtil getJsonArrayfromJSON error:" + e);
			return new JSONArray();
		}
		return new JSONArray();
	}

	protected static ThreadCategory log() {
		return ThreadCategory.getInstance(HttpCollector.class);
	}

	public static String getRestAPIResult(Hashtable<String, Object> config) {
		return null;
	}
}
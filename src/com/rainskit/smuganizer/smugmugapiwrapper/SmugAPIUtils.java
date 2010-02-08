package com.rainskit.smuganizer.smugmugapiwrapper;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

public class SmugAPIUtils {

	private SmugAPIUtils() {}

	public static String getStringSafely(JSONObject parent, String key) throws IOException {
		Object value = getObjectSafely(parent, key);
		return (value == null ? null : value.toString());
	}
	
	public static Integer getIntegerSafely(JSONObject parent, String key) throws IOException {
		Object value = getObjectSafely(parent, key);
		return (value == null ? null : (Integer)value);
	}

	public static Boolean getBooleanSafely(JSONObject parent, String key) throws IOException {
		Object value = getObjectSafely(parent, key);
		return (value == null ? null : (Boolean)value);
	}

	public static Object getObjectSafely(JSONObject parent, String key) throws IOException {
		try {
			if (!parent.isNull(key)) {
				return parent.get(key);
			} else {
				return null;
			}
		} catch (JSONException ex) {
			throw new IOException(ex);
		}
	}
}

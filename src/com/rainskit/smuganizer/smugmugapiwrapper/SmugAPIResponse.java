package com.rainskit.smuganizer.smugmugapiwrapper;

import java.io.IOException;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SmugAPIResponse extends IOException {
	private static final String JSON_OK = "ok";

	private String methodName;

	private String status;
	private Integer errorCode;
	private String errorMessage;

	private JSONObject responseObject;

	public SmugAPIResponse(String responseText, String methodName) throws IOException {
		super();
		this.methodName = methodName;
		
		try {
			responseObject = new JSONObject(responseText);

			status = responseObject.getString("stat");
			if (!JSON_OK.equals(status)) {
				if (!responseObject.isNull("code")) {
					errorCode = Integer.valueOf(responseObject.getInt("code"));
				}
				if (!responseObject.isNull("message")) {
					errorMessage = responseObject.getString("message");
				}
				throw this;
			}
		} catch (JSONException jse) {
			throw new IOException(jse);
		}
	}

	public String getValue(String[] path) throws IOException {
		try {
			return SmugAPIUtils.getStringSafely(traverseParentPath(path), path[path.length - 1]);
		} catch (JSONException ex) {
			throw new IOException("Unable to retrieve string from " + StringUtils.join(path, "/"), ex);
		}
	}

	public Integer getInteger(String[] path) throws IOException {
		try {
			return SmugAPIUtils.getIntegerSafely(traverseParentPath(path), path[path.length - 1]);
		} catch (JSONException ex) {
			throw new IOException("Unable to retrieve integer from " + StringUtils.join(path, "/"), ex);
		}
	}

	public JSONArray getArray(String[] path) throws IOException {
		try {
			return traverseParentPath(path).getJSONArray(path[path.length - 1]);
		} catch (JSONException ex) {
			throw new IOException("Unable to retrieve array from " + StringUtils.join(path, "/"), ex);
		}
	}

	public JSONObject getNestedObject(String[] path) throws IOException {
		try {
			return traverseParentPath(path).getJSONObject(path[path.length - 1]);
		} catch (JSONException ex) {
			throw new IOException("Unable to retrieve object from " + StringUtils.join(path, "/"), ex);
		}
	}

	public JSONObject getResponseObject() {
		return responseObject;
	}

	private JSONObject traverseParentPath(String[] path) throws JSONException {
		JSONObject currentObject = responseObject;
		for (int i = 0; i < path.length - 1; i++) {
				currentObject = currentObject.getJSONObject(path[i]);
		}
		return currentObject;
	}

	public int getErrorCode() {
		return (errorCode == null ? 0 : errorCode.intValue());
	}

	@Override
	public String toString() {
		return methodName + " failed: (code: " + errorCode.toString() + "; message: " + errorMessage;
	}
}

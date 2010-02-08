package com.rainskit.smuganizer.smugmugapiwrapper;

import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.io.IOUtils;

public class SmugAPIMethod {
	private static final String USER_AGENT = "User-Agent";

	private static final String POST_METHOD = "method";
	private static final String POST_API_KEY = "APIKey";
	private static final String POST_SESSION_ID = "SessionID";

	private static final String PUT_CONTENT_LENGTH = "Content-Length";
	private static final String PUT_CONTENT_MD5 = "Content-MD5";
	private static final String PUT_SESSION_ID = "X-Smug-SessionID";
	private static final String PUT_API_VERSION = "X-Smug-Version";
	private static final String PUT_RESPONSE_TYPE = "X-Smug-ResponseType";

	static final HttpClient httpClient = new HttpClient();
	static {
		httpClient.setHttpConnectionManager(new MultiThreadedHttpConnectionManager());
	}

	static String sessionID;

	private String methodName;
	private ArrayList<NameValuePair> parameters;

	public SmugAPIMethod(String methodName) {
		this.methodName = methodName;
		this.parameters = new ArrayList<NameValuePair>();
	}

	public void addParameter(String key, String value) {
		parameters.add(new NameValuePair(key, value));
	}

	public SmugAPIResponse execute() throws IOException {
		PostMethod postMethod = null;
		try {
			postMethod = new PostMethod(SmugAPIConstants.API_URL);
			
			postMethod.setRequestHeader(USER_AGENT, SmugAPIConstants.USER_AGENT);
			postMethod.addParameter(POST_METHOD, methodName);
			postMethod.addParameter(POST_API_KEY, SmugAPIConstants.API_KEY);
			if (sessionID != null) {
				postMethod.addParameter(POST_SESSION_ID, sessionID);
			}
			
			for (NameValuePair each : parameters) {
				postMethod.addParameter(each);
			}

			int code = httpClient.executeMethod(postMethod);
			if (code == HttpStatus.SC_OK) {
				return new SmugAPIResponse(IOUtils.toString(postMethod.getResponseBodyAsStream(),
															postMethod.getResponseCharSet()),
											methodName);
			} else {
				throw new IOException("Error: received non-'200' response code from server while calling " + methodName + " at " + postMethod.getURI() + ".  (Response code was: " + code + ")");
			}
		} finally {
			if (postMethod != null) {
				postMethod.releaseConnection();
			}
		}
	}

	public SmugAPIResponse executeUpload(byte[] imageData, String fileName) throws IOException {
		PutMethod putMethod = null;
		try {
			putMethod = new PutMethod(SmugAPIConstants.API_UPLOAD_URL + URIUtil.encode(fileName, null));

			putMethod.setRequestHeader(USER_AGENT, SmugAPIConstants.USER_AGENT);
			putMethod.setRequestHeader(PUT_CONTENT_LENGTH, Integer.toString(imageData.length));
			putMethod.setRequestHeader(PUT_CONTENT_MD5, DigestUtils.md5Hex(imageData));
			putMethod.setRequestHeader(PUT_SESSION_ID, sessionID);
			putMethod.setRequestHeader(PUT_API_VERSION, SmugAPIConstants.API_VERSION);
			putMethod.setRequestHeader(PUT_RESPONSE_TYPE, SmugAPIConstants.API_TYPE);

			for (NameValuePair each : parameters) {
				putMethod.setRequestHeader(each.getName(), each.getValue());
			}

			putMethod.setRequestEntity(new ByteArrayRequestEntity(imageData));

			int code = httpClient.executeMethod(putMethod);
			if (code == HttpStatus.SC_OK) {
				return new SmugAPIResponse(IOUtils.toString(putMethod.getResponseBodyAsStream(),
															putMethod.getResponseCharSet()),
											methodName);
			} else {
				throw new IOException("Error: received non-'200' response code from server while calling " + methodName + " at " + putMethod.getURI() + ".  (Response code was: " + code + ")");
			}
		} finally {
			if (putMethod != null) {
				putMethod.releaseConnection();
			}
		}
	}
}

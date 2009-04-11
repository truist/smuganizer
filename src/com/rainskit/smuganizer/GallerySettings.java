package com.rainskit.smuganizer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

public class GallerySettings {
	private static final String URL = "gallery.url";
	private static final String USERNAME = "gallery.username";
	private static final String PASSWORD = "gallery.password";
	private static final String NULL_PASSWORD = "";
	
	private static Preferences storage = Preferences.userNodeForPackage(Main.class);
	
	private char[] sessionPassword;
	
	public URL getURL() throws MalformedURLException {
		return new URL(storage.get(URL, "http://"));
	}
	
	public void setURL(URL value) {
		storage.put(URL, value.toExternalForm());
	}

	public String getUsername() {
		return storage.get(USERNAME, "");
	}
	
	public void setUsername(String value) {
		storage.put(USERNAME, value);
	}
	
	public char[] getPassword() {
		if (sessionPassword == null) {
			sessionPassword = storage.get(PASSWORD, NULL_PASSWORD).toCharArray();
		}
		return sessionPassword;
	}
	
	public void setPassword(char[] value, boolean save) {
		sessionPassword = value;
		if (save) {
			storage.put(PASSWORD, String.valueOf(value));
		} else {
			storage.put(PASSWORD, NULL_PASSWORD);
		}
	}

	public boolean isPasswordSaved() {
		return !NULL_PASSWORD.equals(storage.get(PASSWORD, NULL_PASSWORD));
	}
	
	public void addSettingListener(final SettingsListener listener) {
		storage.addPreferenceChangeListener(new PreferenceChangeListener() {
			public void preferenceChange(PreferenceChangeEvent evt) {
				listener.settingChanged(evt.getKey());
			}
		});
	}
}

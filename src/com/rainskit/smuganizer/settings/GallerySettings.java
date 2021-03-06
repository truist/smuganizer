package com.rainskit.smuganizer.settings;

import com.rainskit.smuganizer.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.prefs.Preferences;

public class GallerySettings {
	private static final String URL = "gallery.url";
	private static final String USERNAME = "gallery.username";
	private static final String PASSWORD = "gallery.password";
	private static final String NULL_PASSWORD = "";
	private static final String CHECK_PROTECTED_ALBUMS = "gallery.check_protected_albums";
	private static final String CLEAN_CAPTIONS = "gallery.clean_captions";
	
	private static Preferences storage = Preferences.userNodeForPackage(Main.class);

	private static char[] sessionPassword;
	
	private GallerySettings() {};
	
	public static URL getURL() throws MalformedURLException {
		return new URL(storage.get(URL, "http://"));
	}
	
	public static void setURL(URL value) {
		storage.put(URL, value.toExternalForm());
	}

	public static String getUsername() {
		return storage.get(USERNAME, "");
	}
	
	public static void setUsername(String value) {
		storage.put(USERNAME, value);
	}
	
	public static char[] getPassword() {
		if (sessionPassword == null) {
			sessionPassword = storage.get(PASSWORD, NULL_PASSWORD).toCharArray();
		}
		return sessionPassword;
	}
	
	public static void setPassword(char[] value, boolean save) {
		sessionPassword = value;
		if (save) {
			storage.put(PASSWORD, String.valueOf(value));
		} else {
			storage.put(PASSWORD, NULL_PASSWORD);
		}
	}

	public static boolean isPasswordSaved() {
		return !NULL_PASSWORD.equals(storage.get(PASSWORD, NULL_PASSWORD));
	}
	
	public static boolean getCheckProtectedAlbums() {
		return storage.getBoolean(CHECK_PROTECTED_ALBUMS, true);
	}
	
	public static void setCheckProtectedAlbums(boolean selected) {
		storage.putBoolean(CHECK_PROTECTED_ALBUMS, selected);
	}
	
	public static boolean getCleanCaptions() {
		return storage.getBoolean(CLEAN_CAPTIONS, true);
	}
	
	public static void setCleanCaptions(boolean selected) {
		storage.putBoolean(CLEAN_CAPTIONS, selected);
	}
}

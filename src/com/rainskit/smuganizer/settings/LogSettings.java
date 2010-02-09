package com.rainskit.smuganizer.settings;

import com.rainskit.smuganizer.Main;
import java.util.prefs.Preferences;

public class LogSettings {
	private static String LOG_HEADERS = "log.headers";
	private static String LOG_CONTENT = "log.headers_and_content";

	private static Preferences storage = Preferences.userNodeForPackage(Main.class);

	public static boolean getLogHeaders() {
		return storage.getBoolean(LOG_HEADERS, false);
	}

	public static void setLogHeaders(boolean selected) {
		storage.putBoolean(LOG_HEADERS, selected);
	}

	public static boolean getLogContent() {
		return storage.getBoolean(LOG_CONTENT, false);
	}

	public static void setLogContent(boolean selected) {
		storage.putBoolean(LOG_CONTENT, selected);
	}
}

package com.rainskit.smuganizer.settings;

import com.rainskit.smuganizer.*;
import java.util.prefs.Preferences;

public class FileSettings {
    private static final String PRESERVE_CAPTIONS = "file.preserve_captions";

	private static Preferences storage = Preferences.userNodeForPackage(Main.class);

	private FileSettings() {};

	public static boolean getPreserveCaptions() {
		return storage.getBoolean(PRESERVE_CAPTIONS, true);
	}

	public static void setPreserveCaptions(boolean selected) {
		storage.putBoolean(PRESERVE_CAPTIONS, selected);
	}
}

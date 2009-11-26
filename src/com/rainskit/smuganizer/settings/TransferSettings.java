package com.rainskit.smuganizer.settings;

import com.rainskit.smuganizer.*;
import java.util.prefs.Preferences;

public class TransferSettings {
	private static String IGNORE_EXIF_DESCRIPTIONS = "transfer.ignore_exif_descriptions";
	private static String REMOVE_EXIF_DESCRIPTIONS = "transfer.remove_exif_descriptions";
	
	private static Preferences storage = Preferences.userNodeForPackage(Main.class);

	public static boolean getIgnoreExifDescriptions() {
		return storage.getBoolean(IGNORE_EXIF_DESCRIPTIONS, false);
	}

	public static void setIgnoreExifDescriptions(boolean selected) {
		storage.putBoolean(IGNORE_EXIF_DESCRIPTIONS, selected);
	}

	public static boolean getRemoveExifDescriptions() {
		return storage.getBoolean(REMOVE_EXIF_DESCRIPTIONS, false);
	}
	
	public static void setRemoveExifDescriptions(boolean selected) {
		storage.putBoolean(REMOVE_EXIF_DESCRIPTIONS, selected);
	}
}

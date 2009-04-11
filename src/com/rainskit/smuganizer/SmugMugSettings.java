package com.rainskit.smuganizer;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

public class SmugMugSettings {
	private static final String USERNAME = "smugmug.username";
	private static final String PASSWORD = "smugmug.password";
	private static final String NULL_PASSWORD = "";
	public static final String TREE_SORT = "tree.sort";
	public static final String TREE_CATEGORY_SORT = "tree.categorysort";
	
	private static Preferences storage = Preferences.userNodeForPackage(Main.class);
	
	private static char[] sessionPassword;
	private static SettingsListener listener;
	
	private SmugMugSettings() {}

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
	
	public static boolean getTreeSort() {
		return storage.getBoolean(TREE_SORT, false);
	}
	
	public static void setTreeSort(boolean sort) {
		storage.putBoolean(TREE_SORT, sort);
	}
	
	public static boolean getTreeCategorySort() {
		return storage.getBoolean(TREE_CATEGORY_SORT, true);
	}
	
	public static void setTreeCategorySort(boolean sort) {
		storage.putBoolean(TREE_CATEGORY_SORT, sort);
	}

	public static void setSettingsListener(SettingsListener listener) {
		if (SmugMugSettings.listener == null) {
			storage.addPreferenceChangeListener(new PreferenceChangeListener() {
				public void preferenceChange(PreferenceChangeEvent evt) {
					SmugMugSettings.listener.settingChanged(evt.getKey());
				}
			});
		}
		SmugMugSettings.listener = listener;
	}
}

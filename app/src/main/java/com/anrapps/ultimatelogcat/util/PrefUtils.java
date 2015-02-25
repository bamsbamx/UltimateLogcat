package com.anrapps.ultimatelogcat.util;
import android.content.SharedPreferences;
import android.content.Context;
import android.preference.PreferenceManager;

public class PrefUtils {
	
	private static final String PREF_KEY_WIZARD_DONE = "pref_wizard_done";
	
	public static boolean isWizardDone(Context c) {
		return sp(c).getBoolean(PREF_KEY_WIZARD_DONE, false);
	}
	
	public static void setWizardDone(Context c, boolean done) {
		sp(c).edit().putBoolean(PREF_KEY_WIZARD_DONE, done).apply();
	}
	
	private static SharedPreferences sp(Context c) {
		return PreferenceManager.getDefaultSharedPreferences(c);
	}
}

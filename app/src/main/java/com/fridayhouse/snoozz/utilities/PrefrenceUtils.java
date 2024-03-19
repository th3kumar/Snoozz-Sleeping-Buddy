package com.fridayhouse.snoozz.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

public class PrefrenceUtils {

    static String masterKeyAlias;
    static SharedPreferences sharedPreferences, sharedPreferencesLang;
    private static final String PREF_NAME = "saved_user_data";
    private static final String PREF_NAME_LANGUAGE = "language_data_saved";

    // use the shared preferences and editor as you normally would
    static SharedPreferences.Editor editor, editorLang;

    public static SharedPreferences getPrefs(Context context) {
        try {
            sharedPreferences = context.getSharedPreferences(PREF_NAME, 0);
            editor = sharedPreferences.edit();
        } catch (Exception exception) {
            exception.getMessage();
        }
        return sharedPreferences;
    }

    public static SharedPreferences getPrefslanguage(Context context) {
        try {
            sharedPreferencesLang = context.getSharedPreferences(PREF_NAME_LANGUAGE, 0);
            editorLang = sharedPreferences.edit();
        } catch (Exception exception) {
            exception.getMessage();
        }
        return sharedPreferencesLang;
    }

    public static void insertData(Context context, String key, String value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void insertDataLang(Context context, String key, String value) {
        SharedPreferences.Editor editor = getPrefslanguage(context).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void insertLongData(Context context, String key, int value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static void insertDataInBoolean(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void insertDataInBooleanIsUpdate(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = getPrefslanguage(context).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }


    public static void insertStringSet(Context context, String key, Set<String> value) {
        getPrefs(context).edit().putStringSet(key, value).apply();
    }

    public static String retriveData(Context context, String key) {
        return getPrefs(context).getString(key, "");
    }

    public static String retriveLangData(Context context, String key) {
        return getPrefslanguage(context).getString(key, "");
    }

    public static boolean retriveDataInBoolean(Context context, String key) {
        return getPrefs(context).getBoolean(key, false);
    }

    public static boolean retriveDataInBooleanIsUpdate(Context context, String key) {
        return getPrefslanguage(context).getBoolean(key, false);
    }

    public static long retriveLongData(Context context, String key) {
        return getPrefs(context).getInt(key, 0);
    }

    public static Set<String> getStringSet(Context context, String key) {
        return getPrefs(context).getStringSet(key, null);
    }

    public static void deleteData(Context context, String key) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.remove(key);
        editor.apply();
    }

    public static boolean checkContains(Context ctx, String key) {
        boolean isContain = getPrefs(ctx).contains(key);
        return isContain;
    }

}


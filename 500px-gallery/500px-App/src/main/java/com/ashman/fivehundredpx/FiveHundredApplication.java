package com.ashman.fivehundredpx;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.ashman.fivehundredpx.api.auth.AccessToken;
import com.ashman.fivehundredpx.enums.GalleryCategory;
import com.ashman.fivehundredpx.enums.PhotoSize;

public class FiveHundredApplication extends Application {
    private static final String PREF_FILE = "Preferences";
    private static final String ACCESS_TOKEN = "AccessToken";
    private static final String ACCESS_TOKEN_SECRET = "AccessTokenSecret";
    private static final String PHOTO_SIZE = "PhotoSize";
    private static FiveHundredApplication instance;

    public static final GalleryCategory DEFAULT_CATEGORY = GalleryCategory.UNCATEGORIZED;
    public static final PhotoSize DEFAULT_PHOTOSIZE = PhotoSize.LARGE;

    private boolean isDebug = false;

    public static FiveHundredApplication get() {
        return instance;
    }

    public void onCreate() {
        super.onCreate();
        instance = this;

        //Determine if the app is currently in debug mode
        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(
                    getApplicationContext().getPackageName(), 0);
            isDebug = (pinfo.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            //Should not happen, bug if it does leave isDebug to it's default value
        }
    }

    public boolean isDebug() {
        return isDebug;
    }

    public void saveAccessToken(AccessToken token) {
        SharedPreferences.Editor editor = getSharedPreferences(PREF_FILE, MODE_PRIVATE).edit();
        if(token == null) {
            editor.putString(ACCESS_TOKEN, null);
            editor.putString(ACCESS_TOKEN_SECRET, null);
        } else {
            editor.putString(ACCESS_TOKEN, token.getToken());
            editor.putString(ACCESS_TOKEN_SECRET, token.getTokenSecret());
        }
        editor.commit();
    }

    public AccessToken getAccessToken() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        String token = sharedPreferences.getString(ACCESS_TOKEN, null);
        String secret = sharedPreferences.getString(ACCESS_TOKEN_SECRET, null);
        if(token == null || secret == null)
            return null;
        return new AccessToken(token, secret);
    }

    public boolean isLoggedIn() {
        return getAccessToken() != null;
    }

    public PhotoSize getDefaultPhotoSize() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        int ordinal = sharedPreferences.getInt(PHOTO_SIZE, DEFAULT_PHOTOSIZE.ordinal());
        return PhotoSize.values()[ordinal];
    }

    public void saveDefaultPhotoSize(PhotoSize photoSize) {
        SharedPreferences.Editor editor = getSharedPreferences(PREF_FILE, MODE_PRIVATE).edit();
        editor.putInt(PHOTO_SIZE, photoSize.ordinal());
        editor.commit();
    }
}

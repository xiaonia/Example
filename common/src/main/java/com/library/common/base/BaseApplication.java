package com.library.common.base;

import com.library.common.utils.Utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.util.UUID;

public class BaseApplication extends Application implements ApplicationContext {

    private static BaseApplication sInstance;

    private int mVersionCode;
    private String mVersionName;
    private String mSignature;
    private String mChannelName;
    private String mDeviceId;
    private String mDeviceSn;
    private String mDeviceInfo;
    private String mUploadKey;
    private Context mAppContext;

    private SharedPreferences mPreferences;

    public static BaseApplication getInstance() {
        return sInstance;
    }

    public void setApplicationContext(Context context) {
        mAppContext = context;
    }

    @Override
    public Context getApplicationContext() {
        if (mAppContext != null) {
            return mAppContext;
        }

        return super.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    @Override
    public SharedPreferences getPreferences() {
        if (mPreferences == null) {
            mPreferences = getApplicationContext().getSharedPreferences(getPreferencesName(), MODE_PRIVATE);
        }
        return mPreferences;
    }

    /**
     * @return app shared preference name.
     */
    protected String getPreferencesName() {
        return getApplicationContext().getPackageName();
    }

    /**
     * @return the application's version code set in the manifest.
     */
    public int getVersionCode() {
        checkVersionInfo();
        return mVersionCode;
    }

    /**
     * @return the application's version name set in the manifest.
     */
    public String getVersionName() {
        checkVersionInfo();
        return mVersionName;
    }

    /**
     * The version info with the format that the pot separated version name and version code,
     * as "dove.999999".
     */
    public String getVersionInfo() {
        checkVersionInfo();
        return mVersionName + "." + mVersionCode;
    }

    /**
     * @return The UMeng channel name, default is 'test'.
     */
    public String getChannelName() {
        if (mChannelName == null) {
            mChannelName = getMetaData("UMENG_CHANNEL", "test");
        }
        return mChannelName;
    }

    /**
     * @return The UMeng app key, default is empty.
     */
    public String getAppKey() {
        return getMetaData("UMENG_APPKEY", "");
    }

    /**
     * @return The signature of this app.
     */
    public String getSignature() {
        if (TextUtils.isEmpty(mSignature)) {
            mSignature = generateSignature();
        }
        return mSignature;
    }

    /**
     * Get meta data with specified key. if not found, defValue will be returned.
     */
    public String getMetaData(String key, String defValue) {
        try {
            final Bundle bundle = getPackageManager()
                    .getApplicationInfo(getPackageName(),
                            PackageManager.GET_META_DATA).metaData;
            return String.valueOf(bundle.get(key));
        } catch (Exception ignored) {
        }
        return defValue;
    }

    /**
     * @return The unique device id.
     */
    public String getDeviceId() {
        if (mDeviceId == null) {
            mDeviceId = generateDeviceId();
        }
        return mDeviceId;
    }

    /**
     * @return Device serial number.
     */
    public String getDeviceSn() {
        if (mDeviceSn == null) {
            mDeviceSn = generateDeviceSn();
        }
        return mDeviceSn;
    }

    /**
     * @return Device info.
     */
    public String getDeviceInfo() {
        if (mDeviceInfo == null) {
            mDeviceInfo = generateDeviceInfo();
        }
        return mDeviceInfo;
    }

    /**
     * @return Upload key
     */
    public String getUploadKey() {
        if (mUploadKey == null) {
            String sig = getSignature();
            if (sig.length() < 8) {
                sig = getPackageName();
            }
            if (sig.length() % 8 > 0) {
                sig = sig.substring(0, sig.length() - sig.length() % 8);
            }
            mUploadKey = sig;
        }
        return mUploadKey;
    }

    /**
     * @return true if the device is tablet, otherwise false.
     */
    public boolean isTablet() {
        final TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        return tm.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE;
    }

    public boolean hasNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable();
    }

    private void checkVersionInfo() {
        if (mVersionName == null || mVersionCode == 0) {
            try {
                final PackageInfo packageInfo = getPackageManager()
                        .getPackageInfo(getApplicationInfo().packageName, 0);
                if (packageInfo != null) {
                    mVersionName = packageInfo.versionName;
                    mVersionCode = packageInfo.versionCode;
                }
            } catch (Exception ignored) {
            }
        }
    }

    private String generateDeviceId() {
        final SharedPreferences sp = getPreferences();
        String result = sp.getString("device_id", null);
        if (!TextUtils.isEmpty(result)) {
            return result;
        }

        if (TextUtils.isEmpty(result)) {
            final TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            result = tm.getDeviceId();
            if (TextUtils.isEmpty(result)) {
                result = tm.getSimSerialNumber();
            }
        }

        if (TextUtils.isEmpty(result)) {
            result = UUID.randomUUID().toString();
        }

        result = Utils.getMd5(result);
        sp.edit().putString("device_id", result).apply();

        return result;
    }

    private String generateDeviceSn() {
        String result = getPreferences().getString("device_sn", null);
        if (!TextUtils.isEmpty(result)) {
            return result;
        }

        final TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        result = tm.getDeviceId();

        if (TextUtils.isEmpty(result)) {
            final char[] idPool = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
            for (int i = 0; i < 10; i++) {
                int randomIdx = (int) (Math.random() * idPool.length);
                if (randomIdx > idPool.length - 1) {
                    continue;
                }
                result += idPool[randomIdx];
            }
        }
        getPreferences().edit().putString("device_sn", result).apply();
        return result;
    }

    private String generateDeviceInfo() {
        final TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        final String imei = tm.getDeviceId();
        final String imsi = tm.getSubscriberId();
        final String model = android.os.Build.MODEL;
        final String brand = android.os.Build.BRAND;
        final String cellNum = tm.getLine1Number();
        return String.format("platform=android&imei=%s&imsi=%s&model=%s&brand=%s&cellnum=%s",
                imei, imsi, Uri.encode(model), Uri.encode(brand), cellNum);
    }

    private String generateSignature() {
        final String token = getPreferences().getString("token", "");
        return Utils.encrypt(token, getPackageName());
    }
}

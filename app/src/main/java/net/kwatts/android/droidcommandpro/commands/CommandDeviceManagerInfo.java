package net.kwatts.android.droidcommandpro.commands;
import android.app.KeyguardManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;

import net.kwatts.android.droidcommandpro.AdbshellkitApiReceiver;
import net.kwatts.android.droidcommandpro.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import timber.log.Timber;


public class CommandDeviceManagerInfo {


    public static String cmd = "cmd_device_policy_manager_info";
    public static String[] permissions = { "" };


    public static void onReceive(final AdbshellkitApiReceiver apiReceiver, final Context context, final Intent intent) {
        //final String application_name = intent.getStringExtra("application_name");
        ResultReturner.returnData(apiReceiver, intent, out -> {
            JSONObject res = run(context);
            out.print(res.toString(1));
        });
    }

    public static JSONObject run(android.content.Context ctx) {
        JSONObject res = new JSONObject();
        try {
            res.put("devicepolicymanager.global", getDevicePolicyManagerGlobalData(ctx));
            res.put("devicepolicymanager", getDevicePolicyManagerData(ctx));
        } catch (JSONException e) {

        }
        return res;
    }

    public static JSONArray getDevicePolicyManagerData(Context ctx) {
        JSONArray devicePolicyManager = new JSONArray();
        Boolean isActive = false;
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                PackageManager pm = ctx.getPackageManager();
                DevicePolicyManager activeDevicePolicyManager = (DevicePolicyManager) ctx.getSystemService(Context.DEVICE_POLICY_SERVICE);
                List<ComponentName> activeAdmins = activeDevicePolicyManager.getActiveAdmins();
                if (activeDevicePolicyManager != null) {
                    List<ResolveInfo> availableAdmin = ctx.getPackageManager().queryBroadcastReceivers(
                            new Intent(DeviceAdminReceiver.ACTION_DEVICE_ADMIN_ENABLED),
                            PackageManager.GET_META_DATA);
                    Timber.e("queryBroadcastReceivers(): " + availableAdmin.size());
                    for (int i = 0, count = availableAdmin.size(); i < count; i++) {
                        JSONObject adminApps = new JSONObject();
                        ResolveInfo ri = availableAdmin.get(i);
                        String adminName = ri.toString();
                        int type;
                        ActivityInfo ai = ri.activityInfo;
                        adminApps.put("package_name", ai.packageName);
                        XmlResourceParser parser = ai.loadXmlMetaData(ctx.getPackageManager(), DeviceAdminReceiver.DEVICE_ADMIN_META_DATA);
                        try {
                            ArrayList<String> policiesUsed = new ArrayList<>();
                            int outerDepth = parser.getDepth();
                            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                                    && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
                                String tagName = parser.getName();
                                if (tagName != null && tagName.equals("uses-policies")) {
                                    int innerDepth = parser.getDepth();
                                    while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                                            && (type != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
                                        if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                                            continue;
                                        }
                                        String policyName = parser.getName();
                                        policiesUsed.add(policyName);
                                    }
                                }
                            }
                            JSONArray policiesUsedJson = new JSONArray(policiesUsed);
                            adminApps.put("policiesused", policiesUsedJson);
                        } catch (Exception e) {
                            Timber.e( e);
                        }
                        for (ComponentName admin : activeAdmins) {
                            if (admin.getPackageName().equals(ai.packageName)) {
                                isActive = true;
                                adminApps.put("isdeviceownerapp", activeDevicePolicyManager.isDeviceOwnerApp(admin.getPackageName()));
                                adminApps.put("getpasswordexpiration", activeDevicePolicyManager.getPasswordExpiration(admin));
                                adminApps.put("getpasswordexpirationtimeout", activeDevicePolicyManager.getPasswordExpirationTimeout(admin));
                                adminApps.put("getpasswordminimumlength", activeDevicePolicyManager.getPasswordMinimumLength(admin));
                                adminApps.put("getpasswordminimumletters", activeDevicePolicyManager.getPasswordMinimumLetters(admin));
                                adminApps.put("getpasswordminimumlowercase", activeDevicePolicyManager.getPasswordMinimumLowerCase(admin));
                                adminApps.put("getpasswordminimumnonletter", activeDevicePolicyManager.getPasswordMinimumNonLetter(admin));
                                adminApps.put("getpasswordminimumnumeric", activeDevicePolicyManager.getPasswordMinimumNumeric(admin));
                                adminApps.put("getpasswordminimumsymbols", activeDevicePolicyManager.getPasswordMinimumSymbols(admin));
                                adminApps.put("getpasswordminimummppercase", activeDevicePolicyManager.getPasswordMinimumUpperCase(admin));
                                adminApps.put("getpasswordquality", activeDevicePolicyManager.getPasswordQuality(admin));
                                adminApps.put("getkeyguarddisabledfeatures", activeDevicePolicyManager.getKeyguardDisabledFeatures(admin));
                                break;
                            }
                        }
                        adminApps.put("isActive", isActive);
                        isActive = false;
                        devicePolicyManager.put(adminApps);
                    }
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        return devicePolicyManager;
    }

    public static JSONObject getDevicePolicyManagerGlobalData(Context ctx) {
        JSONObject adminApps = new JSONObject();
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                PackageManager pm = ctx.getPackageManager();
                DevicePolicyManager activeDevicePolicyManager = (DevicePolicyManager) ctx.getSystemService(Context.DEVICE_POLICY_SERVICE);
                adminApps.put("storage_encryption_status", activeDevicePolicyManager.getStorageEncryptionStatus());
                adminApps.put("screen_capture_disabled", activeDevicePolicyManager.getScreenCaptureDisabled(null));
                adminApps.put("password_minimum_symbols", Integer.toString(activeDevicePolicyManager.getPasswordMinimumSymbols(null)));
                adminApps.put("password_minimum_numeric", Integer.toString(activeDevicePolicyManager.getPasswordMinimumNumeric(null)));
                adminApps.put("password_minimum_nonletter", Integer.toString(activeDevicePolicyManager.getPasswordMinimumNonLetter(null)));
                adminApps.put("password_minimum_lowercase", Integer.toString(activeDevicePolicyManager.getPasswordMinimumLowerCase(null)));
                adminApps.put("password_minimum_uppercase", Integer.toString(activeDevicePolicyManager.getPasswordMinimumUpperCase(null)));
                adminApps.put("password_quality", Integer.toString(activeDevicePolicyManager.getPasswordQuality(null)));
                adminApps.put("password_minimum_letters", Integer.toString(activeDevicePolicyManager.getPasswordMinimumLetters(null)));
                adminApps.put("password_minimum_length", Integer.toString(activeDevicePolicyManager.getPasswordMinimumLength(null)));
                adminApps.put("password_history_length", Integer.toString(activeDevicePolicyManager.getPasswordHistoryLength(null)));
                adminApps.put("password_expiration_timeout", Long.toString(activeDevicePolicyManager.getPasswordExpirationTimeout(null)));
                adminApps.put("password_expiration", Long.toString(activeDevicePolicyManager.getPasswordExpiration(null)));
                adminApps.put("maximum_time_to_lock", Long.toString(activeDevicePolicyManager.getMaximumTimeToLock(null)));
                adminApps.put("keyguard_disabled_feature", Integer.toString(activeDevicePolicyManager.getKeyguardDisabledFeatures(null)));
                adminApps.put("camera_disabled",activeDevicePolicyManager.getCameraDisabled(null));
                adminApps.put("auto_time_required",activeDevicePolicyManager.getAutoTimeRequired());
                adminApps.put("maximum_failed_password_wipe", Integer.toString(activeDevicePolicyManager.getMaximumFailedPasswordsForWipe(null)));
                adminApps.put("screen_capture_disabled", activeDevicePolicyManager.getScreenCaptureDisabled(null));
                adminApps.put("storage_encryption",activeDevicePolicyManager.getStorageEncryption(null));
                adminApps.put("is_unistall_blocked",activeDevicePolicyManager.isUninstallBlocked(null,null));

            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        return adminApps;
    }
}

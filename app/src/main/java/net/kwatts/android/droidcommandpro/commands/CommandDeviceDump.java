package net.kwatts.android.droidcommandpro.commands;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.provider.Settings;

import net.kwatts.android.droidcommandpro.ApiReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public class CommandDeviceDump {

    public static String cmd = "device_dump";
    public static String descr = "Dumps device information";
    public static String args = "";
    public static String[] permissions = {};

    public static void onReceive(final ApiReceiver apiReceiver, final Context context, final Intent intent) {
        //final String application_name = intent.getStringExtra("application_name");
        ResultReturner.returnData(apiReceiver, intent, out -> {
            JSONObject res = run(context);
            out.print(res.toString(1));
        });
    }

    public static JSONObject run(android.content.Context ctx) {
        JSONObject res = new JSONObject();

        boolean developer_mode_enabled = Settings.Secure.getInt(ctx.getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1;
        boolean usb_debugging_enabled = Settings.Secure.getInt(ctx.getContentResolver(),
                Settings.Global.ADB_ENABLED, 0) == 1;
        boolean sideloading_enabled = Settings.Secure.getInt(ctx.getContentResolver(),
                Settings.Secure.INSTALL_NON_MARKET_APPS, 0) == 1;

        try {
            res.put("device.policymanager.global", getDevicePolicyManagerGlobalData(ctx));
            res.put("device.policymanager", getDevicePolicyManagerData(ctx));
            res.put("device.os.build", getOSBuildData());
            res.put("settings.global.development_settings_enabled", developer_mode_enabled);
            res.put("settings.global.adb_enabled", usb_debugging_enabled);
            res.put("settings.secure.install_non_market_apps", sideloading_enabled);
            try {
                res.put("settings.global", getAndroidGlobalSettings(ctx));
            } catch (Exception e) {
            }
            try {
                res.put("settings.secure", getAndroidSecureSettings(ctx));
            } catch (Exception e) {
            }

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
                            Timber.e(e);
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
                adminApps.put("camera_disabled", activeDevicePolicyManager.getCameraDisabled(null));
                adminApps.put("auto_time_required", activeDevicePolicyManager.getAutoTimeRequired());
                adminApps.put("maximum_failed_password_wipe", Integer.toString(activeDevicePolicyManager.getMaximumFailedPasswordsForWipe(null)));
                adminApps.put("screen_capture_disabled", activeDevicePolicyManager.getScreenCaptureDisabled(null));
                adminApps.put("storage_encryption", activeDevicePolicyManager.getStorageEncryption(null));
                adminApps.put("is_unistall_blocked", activeDevicePolicyManager.isUninstallBlocked(null, null));

            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        return adminApps;
    }

    public static JSONObject getOSBuildData() {
        JSONObject osBuildInfo = new JSONObject();
        String manufacturerval = Build.MANUFACTURER;
        String modelval = Build.MODEL;
        String osVersionReleaseval = android.os.Build.VERSION.RELEASE;
        int versionSDKINTval = android.os.Build.VERSION.SDK_INT;


        try {
            if (Build.VERSION.SDK_INT >= 23) {
                String device_security_patch = Build.VERSION.SECURITY_PATCH;
                osBuildInfo.put("device_security_patch", device_security_patch);
                osBuildInfo.put("manufacturer", manufacturerval);
                osBuildInfo.put("model", modelval);
                osBuildInfo.put("version.sdk_int", Integer.toString(versionSDKINTval));
                osBuildInfo.put("fingerprint", Build.FINGERPRINT);
                osBuildInfo.put("serial", Build.SERIAL);
                osBuildInfo.put("bootloader", Build.BOOTLOADER);
                osBuildInfo.put("board", Build.BOARD);
                osBuildInfo.put("brand", Build.BRAND);
                osBuildInfo.put("device", Build.DEVICE);
                osBuildInfo.put("display", Build.DISPLAY);
                osBuildInfo.put("hardware", Build.HARDWARE);
                osBuildInfo.put("host", Build.HOST);
                osBuildInfo.put("id", Build.ID);
                JSONArray supported_32_array = new JSONArray(Arrays.asList(clean(Build.SUPPORTED_32_BIT_ABIS)));
                osBuildInfo.put("supported_32_bit_abis", supported_32_array);
                JSONArray supported_64_array = new JSONArray(Arrays.asList(clean(Build.SUPPORTED_64_BIT_ABIS)));
                osBuildInfo.put("supported_64_bit_abis", supported_64_array);
                JSONArray supported_array = new JSONArray(Arrays.asList(clean(Build.SUPPORTED_ABIS)));
                osBuildInfo.put("supported_abis", supported_array);
                osBuildInfo.put("tags", Build.TAGS);
                osBuildInfo.put("type", Build.TYPE);
                osBuildInfo.put("user", Build.USER);
                osBuildInfo.put("time", Build.TIME);
            }

        } catch (JSONException e) {
            Timber.e("Exception while converting os.build.* to JSON, msg: " + e.getMessage());
        }

        return osBuildInfo;
    }

    public static JSONObject getAndroidGlobalSettings(Context ctx) {
        JSONObject androidGlobalSettings = new JSONObject();
        try {
            if (Build.VERSION.SDK_INT >= 17) {
                androidGlobalSettings.put("adb_enabled", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.ADB_ENABLED));
                androidGlobalSettings.put("airplane_mode_on", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON));
                androidGlobalSettings.put("airplane_mode_radios", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.AIRPLANE_MODE_RADIOS));
                androidGlobalSettings.put("always_finish_activities", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.ALWAYS_FINISH_ACTIVITIES));
                androidGlobalSettings.put("animator_duration_scale", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE));
                androidGlobalSettings.put("auto_time", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.AUTO_TIME));
                androidGlobalSettings.put("auto_time_zone", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.AUTO_TIME_ZONE));
                androidGlobalSettings.put("bluetooth_on", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.BLUETOOTH_ON));
                androidGlobalSettings.put("data_roaming", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.DATA_ROAMING));
                androidGlobalSettings.put("debug_app", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.DEBUG_APP));
                androidGlobalSettings.put("development_settings_enabled", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED));
                androidGlobalSettings.put("device_provisioned", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.DEVICE_PROVISIONED));
                androidGlobalSettings.put("http_proxy", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.HTTP_PROXY));
                androidGlobalSettings.put("mode_ringer", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.MODE_RINGER));
                androidGlobalSettings.put("network_preference", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.NETWORK_PREFERENCE));
                androidGlobalSettings.put("radio_bluetooth", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.RADIO_BLUETOOTH));
                androidGlobalSettings.put("radio_cell", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.RADIO_CELL));
                androidGlobalSettings.put("radio_nfc", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.RADIO_NFC));
                androidGlobalSettings.put("radio_wifi", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.RADIO_WIFI));
                androidGlobalSettings.put("show_processes", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.SHOW_PROCESSES));
                androidGlobalSettings.put("stay_on_while_plugged_in", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.STAY_ON_WHILE_PLUGGED_IN));
                androidGlobalSettings.put("transition_animation_scale", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.TRANSITION_ANIMATION_SCALE));
                androidGlobalSettings.put("usb_mass_storage_enabled", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.USB_MASS_STORAGE_ENABLED));
                androidGlobalSettings.put("use_google_mail", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.USE_GOOGLE_MAIL));
                androidGlobalSettings.put("wait_for_debugger", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.WAIT_FOR_DEBUGGER));
                androidGlobalSettings.put("wifi_device_owner_configs_lockdown", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.WIFI_DEVICE_OWNER_CONFIGS_LOCKDOWN));
                androidGlobalSettings.put("wifi_max_dhcp_retry_count", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.WIFI_MAX_DHCP_RETRY_COUNT));
                androidGlobalSettings.put("wifi_mobile_data_transition_wakelock_timeout_ms", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.WIFI_MOBILE_DATA_TRANSITION_WAKELOCK_TIMEOUT_MS));
                androidGlobalSettings.put("wifi_networks_available_notification_on", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON));
                androidGlobalSettings.put("wifi_networks_available_repeat_delay", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.WIFI_NETWORKS_AVAILABLE_REPEAT_DELAY));
                androidGlobalSettings.put("wifi_num_open_networks_kept", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.WIFI_NUM_OPEN_NETWORKS_KEPT));
                androidGlobalSettings.put("wifi_on", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.WIFI_ON));
                androidGlobalSettings.put("wifi_sleep_policy", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.WIFI_SLEEP_POLICY));
                int value = Settings.Global.getInt(ctx.getContentResolver(), Integer.toString(Settings.Global.WIFI_SLEEP_POLICY_DEFAULT
                ), 0);
                androidGlobalSettings.put("wifi_sleep_policy_default", Integer.toString(value));
                value = Settings.Global.getInt(ctx.getContentResolver(), Integer.toString(Settings.Global.WIFI_SLEEP_POLICY_NEVER), 0);
                androidGlobalSettings.put("wifi_sleep_policy_never", Integer.toString(value));
                value = Settings.Global.getInt(ctx.getContentResolver(), Integer.toString(Settings.Global.WIFI_SLEEP_POLICY_NEVER_WHILE_PLUGGED), 0);
                androidGlobalSettings.put("wifi_sleep_policy_never_while_plugged", Integer.toString(value));
                androidGlobalSettings.put("wifi_watchdog_on", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.WIFI_WATCHDOG_ON));
                androidGlobalSettings.put("window_animation_scale", Settings.Global.getString(ctx.getContentResolver(), Settings.Global.WINDOW_ANIMATION_SCALE));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return androidGlobalSettings;
    }

    public static JSONObject getAndroidSecureSettings(Context ctx) {
        JSONObject androidSecureSettings = new JSONObject();
        try {
            if (Build.VERSION.SDK_INT >= 17) {
                androidSecureSettings.put("accessibility_display_inversion_enabled", Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED));
                androidSecureSettings.put("accessibility_enabled", Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED));
                androidSecureSettings.put("accessibility_speak_password", Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ACCESSIBILITY_SPEAK_PASSWORD));
                androidSecureSettings.put("allowed_geolocation_origins", Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ALLOWED_GEOLOCATION_ORIGINS));
                androidSecureSettings.put("android_id", Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID));
                androidSecureSettings.put("default_input_method", Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD));
                androidSecureSettings.put("enabled_accessibility_services", Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES));
                androidSecureSettings.put("enabled_input_methods", Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ENABLED_INPUT_METHODS));
                androidSecureSettings.put("input_method_selector_visibility", Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.INPUT_METHOD_SELECTOR_VISIBILITY));
                androidSecureSettings.put("install_non_market_apps", Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS));
                androidSecureSettings.put("location_mode", Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.LOCATION_MODE));

                int value = Settings.Secure.getInt(ctx.getContentResolver(), Integer.toString(Settings.Secure.LOCATION_MODE_BATTERY_SAVING), 0);
                androidSecureSettings.put("location_mode_battery_saving", Integer.toString(value));

                value = Settings.Secure.getInt(ctx.getContentResolver(), Integer.toString(Settings.Secure.LOCATION_MODE_HIGH_ACCURACY), 0);
                androidSecureSettings.put("location_mode-high_accuracy", Integer.toString(value));

                value = Settings.Secure.getInt(ctx.getContentResolver(), Integer.toString(Settings.Secure.LOCATION_MODE_OFF), 0);
                androidSecureSettings.put("location_mode_off", Integer.toString(value));

                value = Settings.Secure.getInt(ctx.getContentResolver(), Integer.toString(Settings.Secure.LOCATION_MODE_SENSORS_ONLY), 0);
                androidSecureSettings.put("location_mode_sensors_only", Integer.toString(value));

                androidSecureSettings.put("parental_control_enabled", Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.PARENTAL_CONTROL_ENABLED));
                androidSecureSettings.put("parental_control_last_update", Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.PARENTAL_CONTROL_LAST_UPDATE));
                androidSecureSettings.put("parental_control_redirect_url", Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.PARENTAL_CONTROL_REDIRECT_URL));
                androidSecureSettings.put("selected_input_method_subtype", Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.SELECTED_INPUT_METHOD_SUBTYPE));
                androidSecureSettings.put("settings_classname", Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.SETTINGS_CLASSNAME));
                androidSecureSettings.put("skip_first_use_hints", Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.SKIP_FIRST_USE_HINTS));
                androidSecureSettings.put("touch_exploration_enabled", Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.TOUCH_EXPLORATION_ENABLED));
                androidSecureSettings.put("tts_default_pitch", Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.TTS_DEFAULT_PITCH));
                androidSecureSettings.put("tts_default_rate", Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.TTS_DEFAULT_RATE));
                androidSecureSettings.put("tts_default_synth", Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.TTS_DEFAULT_SYNTH));
                androidSecureSettings.put("tts_enabled_plugins", Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.TTS_ENABLED_PLUGINS));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return androidSecureSettings;
    }

    public static String[] clean(final String[] v) {
        List<String> list = new ArrayList<String>(Arrays.asList(v));
        list.removeAll(Collections.singleton(null));
        return list.toArray(new String[list.size()]);
    }
}

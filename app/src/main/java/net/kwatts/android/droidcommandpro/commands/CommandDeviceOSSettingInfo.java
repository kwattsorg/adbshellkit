package net.kwatts.android.droidcommandpro.commands;

import org.json.JSONException;
import org.json.JSONObject;
import android.provider.Settings;

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
import android.util.JsonWriter;


import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import net.kwatts.android.droidcommandpro.AdbshellkitApiReceiver;

import java.util.List;

public class CommandDeviceOSSettingInfo implements Command {
    public static String cmd = "cmd_device_os_setting_info";

    public String getCommandName() {
        return cmd;
    }
    public String[] getPermissions() { return new String[] { "" }; }


    public static void onReceive(final AdbshellkitApiReceiver receiver, final Context context, final Intent intent) {

        ResultReturner.returnData(context, intent, new ResultReturner.ResultJsonWriter() {
            public void writeJson(JsonWriter out) throws Exception {
                out.beginObject();
                boolean developer_mode_enabled = Settings.Secure.getInt(context.getContentResolver(),
                        Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1;
                boolean usb_debugging_enabled = Settings.Secure.getInt(context.getContentResolver(),
                        Settings.Global.ADB_ENABLED, 0) == 1;
                boolean sideloading_enabled = Settings.Secure.getInt(context.getContentResolver(),
                        Settings.Secure.INSTALL_NON_MARKET_APPS, 0) == 1;
                out.name("settings.global.development_settings_enabled").value(developer_mode_enabled);
                out.name("settings.global.adb_enabled").value(usb_debugging_enabled);
                out.name("settings.secure.install_non_market_apps").value(sideloading_enabled);
                out.endObject();
            }
        });
    }

    public static JSONObject run(android.content.Context ctx, List<String> args) {
        JSONObject res = new JSONObject();
        boolean developer_mode_enabled = Settings.Secure.getInt(ctx.getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1;
        boolean usb_debugging_enabled = Settings.Secure.getInt(ctx.getContentResolver(),
                Settings.Global.ADB_ENABLED, 0) == 1;
        boolean sideloading_enabled = Settings.Secure.getInt(ctx.getContentResolver(),
                Settings.Secure.INSTALL_NON_MARKET_APPS, 0) == 1;
        try {
            res.put("settings.global.development_settings_enabled", developer_mode_enabled);
            res.put("settings.global.adb_enabled", usb_debugging_enabled);
            res.put("settings.secure.install_non_market_apps", sideloading_enabled);
            try {
                res.put("settings.global", getAndroidGlobalSettings(ctx));
            } catch (Exception e) {}

            try {
                res.put("settings.secure", getAndroidSecureSettings(ctx));
            } catch (Exception e) {}

        } catch (JSONException e) {

        }

        return res;

    }


    public JSONObject execute(android.content.Context ctx, List<String> args) {
        JSONObject res = new JSONObject();

        boolean developer_mode_enabled = Settings.Secure.getInt(ctx.getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1;
        boolean usb_debugging_enabled = Settings.Secure.getInt(ctx.getContentResolver(),
                Settings.Global.ADB_ENABLED, 0) == 1;
        boolean sideloading_enabled = Settings.Secure.getInt(ctx.getContentResolver(),
                Settings.Secure.INSTALL_NON_MARKET_APPS, 0) == 1;

        try {
            res.put("settings.global.development_settings_enabled", developer_mode_enabled);
            res.put("settings.global.adb_enabled", usb_debugging_enabled);
            res.put("settings.secure.install_non_market_apps", sideloading_enabled);
            try {
                res.put("settings.global", getAndroidGlobalSettings(ctx));
            } catch (Exception e) {}

            try {
                res.put("settings.secure", getAndroidSecureSettings(ctx));
            } catch (Exception e) {}

        } catch (JSONException e) {

        }

        return res;
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
}

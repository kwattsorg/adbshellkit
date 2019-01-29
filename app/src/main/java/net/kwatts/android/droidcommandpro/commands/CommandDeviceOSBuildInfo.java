package net.kwatts.android.droidcommandpro.commands;

//https://github.com/kwattsorg/android-nativecommander/blob/master/app/src/main/java/net/kwatts/android/droidcommandpro/commands/CommandDeviceInfo.java
import android.os.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import android.provider.Settings;


import timber.log.Timber;

public class CommandDeviceOSBuildInfo implements Command {

    public static String cmd = "cmd_device_os_build_info";

    public String getCommandName() {
        return cmd;
    }
    public String[] getPermissions() { return new String[] { "" }; }

    public JSONObject execute(android.content.Context ctx, List<String> args) {
        return getOSBuildData();
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

    public static String[] clean(final String[] v) {
        List<String> list = new ArrayList<String>(Arrays.asList(v));
        list.removeAll(Collections.singleton(null));
        return list.toArray(new String[list.size()]);
    }
}
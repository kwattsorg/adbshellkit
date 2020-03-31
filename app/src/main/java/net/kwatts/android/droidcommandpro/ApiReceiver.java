package net.kwatts.android.droidcommandpro;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.JsonWriter;
import android.widget.Toast;

import com.androidhiddencamera.HiddenCameraUtils;


import net.kwatts.android.droidcommandpro.commands.CommandAppRiskReport;
import net.kwatts.android.droidcommandpro.commands.CommandDiscovery;
import net.kwatts.android.droidcommandpro.commands.CommandCamera;
import net.kwatts.android.droidcommandpro.commands.CommandDeviceDump;
import net.kwatts.android.droidcommandpro.commands.CommandDialog;
import net.kwatts.android.droidcommandpro.commands.CommandGetContacts;
import net.kwatts.android.droidcommandpro.commands.CommandMicrophoneRecorder;
import net.kwatts.android.droidcommandpro.commands.CommandProcessTools;
import net.kwatts.android.droidcommandpro.commands.CommandRunSystem;
import net.kwatts.android.droidcommandpro.commands.CommandSmali;
import net.kwatts.android.droidcommandpro.commands.CommandTelephony;
import net.kwatts.android.droidcommandpro.commands.CommandTorch;
import net.kwatts.android.droidcommandpro.commands.CommandUploadFile;
import net.kwatts.android.droidcommandpro.commands.CommandVibrate;
import net.kwatts.android.droidcommandpro.commands.CommandVolume;
import net.kwatts.android.droidcommandpro.commands.ResultReturner;

import java.util.ArrayList;
import java.util.Arrays;

import timber.log.Timber;

// am broadcast --user 0 -n net.kwatts.android.droidcommandpro/.AdbshellkitApiReceiver \
// --es socket_input 1 --es socket_output 2 --es api_method cmd_device_os_setting_info

// https://github.com/termux/termux-api-package/blob/master/termux-api.c
// https://github.com/termux/termux-api/blob/master/app/src/main/java/com/termux/api/ShareAPI.java
// https://github.com/termux/termux-api-package/tree/master/scripts

//TODO: add for adb ones on non root devices
// adb -d shell pm grant net.kwatts.android.droidcommandpro ...
// android.permission.BATTERY_STATS
// android.permission.DUMP
// android.permission.PACKAGE_USAGE_STATS
    /* Shows you the screen on vs. awake ratio. Ideally, the screen on time should be equal to the awake time.
    Find changes in the awake/sleep profile and quickly identify the rogue apps.
    Battery stats shows you the detailed metrics of Doze so you can check how effective battery-saver apps are.
    It can pick out apps that result in partial wakelocks or apps that consume CPU in kernel wakelocks.
    */

//TODO:
// - command that automatically scans network, finding all services, and makes a clean report
// - command that automatically runs bettercap, does mitm, and shows all intercepted packets


public class ApiReceiver extends BroadcastReceiver {

    ArrayList<Class> mCmds = new ArrayList<>(
            Arrays.asList(CommandCamera.class,
                    CommandDeviceDump.class,
                    CommandTorch.class,
                    CommandVibrate.class,
                    CommandTelephony.class,
                    CommandSmali.class,
                    CommandDialog.class,
                    CommandAppRiskReport.class)
    );

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Timber.i("Got the api command in onReceive() ");
            doWork(context, intent);
        } catch (Exception e) {
            // Make sure never to throw exception from BroadCastReceiver to avoid "process is bad"
            // behaviour from the Android system.
            Timber.e("Error in AdbshellkitApiReceiver");
        }
    }

    private void doWork(Context context, Intent intent) {
        String apiMethod = intent.getStringExtra("api_method");
        if (apiMethod == null) {
            Timber.e("Missing 'api_method' extra");
            return;
        }
        Timber.i("apMethod= %s", apiMethod);

        switch (apiMethod) {
            //lifted from https://raw.githubusercontent.com/termux/termux-api/master/app/src/main/java/com/termux/api/TermuxApiReceiver.java
            //todo: check permissions before each call
            case "device_dump":
                CommandDeviceDump.onReceive(this, context, intent);
                break;
            case "app_risk_report":
                CommandAppRiskReport.onReceive(this, context, intent);
                break;
            case "smali":
                CommandSmali.onReceive(this, context, intent);
                break;
            case "upload_file":
                CommandUploadFile.onReceive(this, context, intent);
                break;
            case "get_contacts":
                CommandGetContacts.onReceive(this, context, intent);
                break;
            case "run_system":
                CommandRunSystem.onReceive(this, context, intent);
                break;
            case "telephony_call":
                if (ApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.CALL_PHONE)) {
                    CommandTelephony.onReceiveTelephonyCall(this, context, intent);
                }
                break;
            case "telephony_cell_info":
                if (ApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    CommandTelephony.onReceiveTelephonyCellInfo(this, context, intent);
                }
                break;
            case "telephony_device_info":
                if (ApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.READ_PHONE_STATE)) {
                    CommandTelephony.onReceiveTelephonyDeviceInfo(this, context, intent);
                }
                break;
            case "vibrate":
                CommandVibrate.onReceive(this, context, intent);
                break;
            case "torch":
                CommandTorch.onReceive(this, context, intent);
                break;
            case "volume":
                CommandVolume.onReceive(this, context, intent);
                break;
            case "dialog":
                context.startActivity(new Intent(context, CommandDialog.class).putExtras(intent.getExtras()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case "microphone_recorder":
                if (ApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.RECORD_AUDIO)) {
                    CommandMicrophoneRecorder.onReceive(context, intent);
                }
                break;
            case "camera":
                if (ApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.CAMERA)) {
                    if (HiddenCameraUtils.canOverDrawOtherApps(context)) {
                        CommandCamera.onReceive(context, intent);
                    } else {
                        ResultReturner.returnData(context, intent, new ResultReturner.ResultJsonWriter() {
                            @Override
                            public void writeJson(JsonWriter out) throws Exception {
                                String errorMessage = "Please allow this app to draw over apps by going to 'Settings -> Apps -> ADB Shellkit -> Advanced' and allowing 'Display over other apps' and run this command again.";
                                out.beginObject().name("error").value(errorMessage).endObject();
                            }
                        });
                        Toast.makeText(context, "To take pictures silently you need to select 'ADB Shellkit' allowing draw over apps and run the command again",
                                Toast.LENGTH_LONG).show();
                        HiddenCameraUtils.openDrawOverPermissionSetting(context);
                    }
                }
                break;
            case "process_usage_stats":
                try {
                    PackageManager packageManager = context.getPackageManager();
                    ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
                    AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                    int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
                    if (mode != AppOpsManager.MODE_ALLOWED) {
                        ResultReturner.returnData(context, intent, new ResultReturner.ResultJsonWriter() {
                            @Override
                            public void writeJson(JsonWriter out) throws Exception {
                                String errorMessage = "Please allow this app to access the foreground task by going to Settings -> Security -> 'App With Usage Access' and try again.";
                                out.beginObject().name("error").value(errorMessage).endObject();
                            }
                        });

                        Intent i = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                        context.startActivity(i);
                    } else {
                        CommandProcessTools.onReceiveUsageStats(this, context, intent);

                    }

                } catch (PackageManager.NameNotFoundException e) {
                    //
                }

                break;

            case "process_dump":
                CommandProcessTools.onReceiveDumpProcesses(this, context, intent);
                break;
            case "service_dump":
                CommandProcessTools.onReceiveDumpServices(this, context, intent);
                break;
            case "process_kill":
                CommandProcessTools.onReceiveKillProcess(this, context, intent);
                break;
            default:
                Timber.e("Unrecognized 'api_method' extra: '" + apiMethod + "'");
                ResultReturner.returnData(context, intent, out -> {
                    //out.append("unknown command: " + apiMethod).append("\n");
                    out.append(getCommandApiUsage());
                    out.flush();
                    out.close();
                });
        }
    }

    public String getCommandApiUsage() {
        StringBuilder sb = new StringBuilder();
        sb.append("usage:\t adbshellkit <command> <options>\n");
        sb.append("options:\t --es <string> <value>, --ei <number> <value>, --ez <true|false>, --ef <float> <value>\n");
        sb.append("\nExamples:\n");
        sb.append("Silent selfie from the front camera\n");
        sb.append("\tadbshellkit camera --ei camerafacing 1\n");
        sb.append("Show a dialog window containing a web page\n");
        sb.append("\tadbshellkit dialog --es input_method webview --es web_url https://www.google.com\n");
        sb.append("\nAvailable commands:\n");
        for (Class c : mCmds) {
            try {
                sb.append("\n");
                sb.append(c.getField("cmd").get(null).toString());
                sb.append(": ");
                sb.append(c.getField("descr").get(null).toString());
                sb.append("\n\t");
                sb.append(c.getField("args").get(null).toString());
                if (!(mCmds.indexOf(c) == mCmds.size() - 1)) {
                    sb.append("\n");
                }
            } catch (Exception ignored) {
            }
        }

        return sb.toString();
    }
}

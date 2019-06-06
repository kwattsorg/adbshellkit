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

import timber.log.Timber;

import net.kwatts.android.droidcommandpro.commands.*;

// am broadcast --user 0 -n net.kwatts.android.droidcommandpro/.AdbshellkitApiReceiver \
// --es socket_input 1 --es socket_output 2 --es api_method cmd_device_os_setting_info

// https://github.com/termux/termux-api-package/blob/master/termux-api.c
// https://github.com/termux/termux-api/blob/master/app/src/main/java/com/termux/api/ShareAPI.java
// https://github.com/termux/termux-api-package/tree/master/scripts


//TODO:
// - command that automatically scans network, finding all services, and makes a clean report
// - command that automatically runs bettercap, does mitm, and shows all intercepted packets


public class ApiReceiver extends BroadcastReceiver {

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
        Timber.i("apMethod= " + apiMethod);

        switch (apiMethod) {
            //lifted from https://raw.githubusercontent.com/termux/termux-api/master/app/src/main/java/com/termux/api/TermuxApiReceiver.java
            //todo: check permissions before each call
            case "device_dump":
                CommandDeviceDump.onReceive(this, context, intent);
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
                CommandVibrate.onReceive(this,context,intent);
                break;
            case "torch":
                CommandTorch.onReceive(this,context,intent);
                break;
            case "volume":
                CommandVolume.onReceive(this,context,intent);
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
                        CommandProcessTools.onReceiveUsageStats(this,context,intent);

                    }

                } catch (PackageManager.NameNotFoundException e) {
                    //
                }

                break;

            case "process_dump":
                CommandProcessTools.onReceiveDumpProcesses(this,context,intent);
                break;
            case "service_dump":
                CommandProcessTools.onReceiveDumpServices(this,context,intent);
                break;
            case "process_kill":
                CommandProcessTools.onReceiveKillProcess(this,context,intent);
                break;
            default:
                Timber.e("Unrecognized 'api_method' extra: '" + apiMethod + "'");
        }
    }
}

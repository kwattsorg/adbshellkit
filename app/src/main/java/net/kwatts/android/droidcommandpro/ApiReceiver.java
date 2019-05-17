package net.kwatts.android.droidcommandpro;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
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
// - command that automatically takes pictures every x seconds for y amount of times w/out showing camera UI.
//  + drops them in the media folder
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
            case "cmd_device_dump":
                CommandDeviceDump.onReceive(this, context, intent);
                break;
            case "cmd_smali":
                CommandSmali.onReceive(this, context, intent);
                break;
            case "cmd_upload_file":
                CommandUploadFile.onReceive(this, context, intent);
                break;
            case "cmd_get_contacts":
                CommandGetContacts.onReceive(this, context, intent);
                break;
            case "cmd_run_system":
                CommandRunSystem.onReceive(this, context, intent);
                break;
            case "cmd_text_to_speech":
                //if (ApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.RECORD_AUDIO)) {
                CommandTextToSpeech.onReceive(context, intent);
                //}
                break;
            case "cmd_telephony_call":
                if (ApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.CALL_PHONE)) {
                    CommandTelephony.onReceiveTelephonyCall(this, context, intent);
                }
                break;
            case "cmd_telephony_cell_info":
                if (ApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    CommandTelephony.onReceiveTelephonyCellInfo(this, context, intent);
                }
                break;
            case "cmd_telephony_device_info":
                if (ApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.READ_PHONE_STATE)) {
                    CommandTelephony.onReceiveTelephonyDeviceInfo(this, context, intent);
                }
                break;
            case "cmd_vibrate":
                CommandVibrate.onReceive(this,context,intent);
                break;
            case "cmd_torch":
                CommandTorch.onReceive(this,context,intent);
                break;
            case "cmd_volume":
                CommandVolume.onReceive(this,context,intent);
                break;
            case "cmd_dialog":
                context.startActivity(new Intent(context, CommandDialog.class).putExtras(intent.getExtras()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case "cmd_microphone_recorder":
                if (ApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.RECORD_AUDIO)) {
                    CommandMicrophoneRecorder.onReceive(context, intent);
                }
                break;
            case "cmd_camera":
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
            /*
            case "AudioInfo":
                AudioAPI.onReceive(this, context, intent);
                break;
            case "BatteryStatus":
                BatteryStatusAPI.onReceive(this, context, intent);
                break;
            case "Brightness":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.System.canWrite(context)) {
                        TermuxApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.WRITE_SETTINGS);
                        Toast.makeText(context, "Please enable permission for Termux:API", Toast.LENGTH_LONG).show();

                        // user must enable WRITE_SETTINGS permission this special way
                        Intent settingsIntent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        context.startActivity(settingsIntent);
                        return;
                    }
                }
                BrightnessAPI.onReceive(this, context, intent);
                break;
            case "CameraInfo":
                CameraInfoAPI.onReceive(this, context, intent);
                break;
            case "CameraPhoto":
                if (TermuxApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.CAMERA)) {
                    PhotoAPI.onReceive(this, context, intent);
                }
                break;
            case "CallLog":
                if (TermuxApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.READ_CALL_LOG)) {
                    CallLogAPI.onReceive(context, intent);
                }
                break;
            case "Clipboard":
                ClipboardAPI.onReceive(this, context, intent);
                break;
            case "ContactList":
                if (TermuxApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.READ_CONTACTS)) {
                    ContactListAPI.onReceive(this, context, intent);
                }
                break;
            case "Dialog":
                context.startActivity(new Intent(context, DialogActivity.class).putExtras(intent.getExtras()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case "Download":
                DownloadAPI.onReceive(this, context, intent);
                break;
            case "Fingerprint":
                FingerprintAPI.onReceive(context, intent);
                break;
            case "InfraredFrequencies":
                if (TermuxApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.TRANSMIT_IR)) {
                    InfraredAPI.onReceiveCarrierFrequency(this, context, intent);
                }
                break;
            case "InfraredTransmit":
                if (TermuxApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.TRANSMIT_IR)) {
                    InfraredAPI.onReceiveTransmit(this, context, intent);
                }
                break;
            case "JobScheduler":
                JobSchedulerAPI.onReceive(this, context, intent);
                break;
            case "Keystore":
                KeystoreAPI.onReceive(this, intent);
                break;
            case "Location":
                if (TermuxApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    LocationAPI.onReceive(this, context, intent);
                }
                break;
            case "MediaPlayer":
                MediaPlayerAPI.onReceive(context, intent);
                break;
            case "MediaScanner":
                MediaScannerAPI.onReceive(this, context, intent);
                break;
            case "MicRecorder":
                if (TermuxApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.RECORD_AUDIO)) {
                    MicRecorderAPI.onReceive(context, intent);
                }
                break;
            case "NotificationList":
                ComponentName cn = new ComponentName(context, NotificationService.class);
                String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
                final boolean NotificationServiceEnabled = flat != null && flat.contains(cn.flattenToString());
                if (!NotificationServiceEnabled) {
                    Toast.makeText(context,"Please give Termux:API Notification Access", Toast.LENGTH_LONG).show();
                    context.startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                } else {
                    NotificationListAPI.onReceive(this, context, intent);
                }
                break;
            case "Notification":
                NotificationAPI.onReceiveShowNotification(this, context, intent);
                break;
            case "NotificationRemove":
                NotificationAPI.onReceiveRemoveNotification(this, context, intent);
                break;
            case "Sensor":
                SensorAPI.onReceive(context, intent);
                break;
            case "Share":
                ShareAPI.onReceive(this, context, intent);
                break;
            case "SmsInbox":
                if (TermuxApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS)) {
                    SmsInboxAPI.onReceive(this, context, intent);
                }
                break;
            case "SmsSend":
                if (TermuxApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.SEND_SMS)) {
                    SmsSendAPI.onReceive(this, intent);
                }
                break;
            case "StorageGet":
                StorageGetAPI.onReceive(this, context, intent);
                break;
            case "SpeechToText":
                if (TermuxApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.RECORD_AUDIO)) {
                    SpeechToTextAPI.onReceive(context, intent);
                }
                break;
            case "TelephonyCall":
                if (TermuxApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.CALL_PHONE)) {
                    TelephonyAPI.onReceiveTelephonyCall(this, context, intent);
                }
                break;
            case "TelephonyCellInfo":
                if (TermuxApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    TelephonyAPI.onReceiveTelephonyCellInfo(this, context, intent);
                }
                break;
            case "TelephonyDeviceInfo":
                if (TermuxApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.READ_PHONE_STATE)) {
                    TelephonyAPI.onReceiveTelephonyDeviceInfo(this, context, intent);
                }
                break;
            case "TextToSpeech":
                TextToSpeechAPI.onReceive(context, intent);
                break;
            case "Toast":
                ToastAPI.onReceive(context, intent);
                break;
            case "Torch":
                TorchAPI.onReceive(this, context, intent);
                break;
            case "Vibrate":
                VibrateAPI.onReceive(this, context, intent);
                break;
            case "Volume":
                VolumeAPI.onReceive(this, context, intent);
                break;
            case "Wallpaper":
                WallpaperAPI.onReceive(context, intent);
                break;
            case "WifiConnectionInfo":
                WifiAPI.onReceiveWifiConnectionInfo(this, context, intent);
                break;
            case "WifiScanInfo":
                if (TermuxApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    WifiAPI.onReceiveWifiScanInfo(this, context, intent);
                }
                break;
            case "WifiEnable":
                WifiAPI.onReceiveWifiEnable(this, context, intent);
                break;
*/
            default:
                Timber.e("Unrecognized 'api_method' extra: '" + apiMethod + "'");
        }
    }
}

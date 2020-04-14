package net.kwatts.android.droidcommandpro.commands;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Vibrator;

import net.kwatts.android.droidcommandpro.ApiReceiver;


// Painfully lifted from https://github.com/termux/termux-api-package/blob/master/scripts/termux-vibrate
// adbshellkit-api cmd_vibrate --ei duration_ms 2000 --ez force true
public class CommandVibrate {
    public static int MINIMUM_APP_VERSION = 100;
    public static String cmd = "vibrate";
    public static String descr = "Vibrates the phone for a duration of time";
    public static String args = "--ei duration_ms <time to vibrate, 2000 for 2 seconds>, --ez force <true|false>";
    public static String[] permissions = {""};

    public static String usage() {
        return "{\"cmd\":\"" + cmd + "\"," +
                "\"args\":\"duration_ms (int), force (bool)\"}";
    }

    public static void onReceive(ApiReceiver apiReceiver, Context context, Intent intent) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        int milliseconds = intent.getIntExtra("duration_ms", 1000);
        boolean force = intent.getBooleanExtra("force", false);

        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (am.getRingerMode() == AudioManager.RINGER_MODE_SILENT && !force) {
            // Not vibrating since in silent mode and -f/--force option not used.
        } else {
            vibrator.vibrate(milliseconds);
        }

        ResultReturner.noteDone(apiReceiver, intent);
    }

}

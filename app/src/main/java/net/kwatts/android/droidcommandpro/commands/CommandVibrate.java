package net.kwatts.android.droidcommandpro.commands;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Vibrator;
import net.kwatts.android.droidcommandpro.ApiReceiver;


// Painfully lifted from https://github.com/termux/termux-api-package/blob/master/scripts/termux-vibrate
// adbshellkit-api cmd_vibrate --ei duration_ms 2000 --ez force true
public class CommandVibrate {
    public static String cmd = "cmd_vibrate";
    public static String[] permissions = { "" };

    public static String usage() {
        return "{\"cmd\":\"" + cmd + "\"," +
                "\"args\":\"duration_ms (int), force (bool)\"}";
        //    echo "Usage: $SCRIPTNAME [-d duration] [-f]"
        //    echo "Vibrate the device."
        //    echo "  -d duration  the duration to vibrate in ms (default:1000)"
        //    echo "  -f           force vibration even in silent mode"

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

package net.kwatts.android.droidcommandpro;

import android.content.Context;

import java.io.File;
import java.io.IOException;

import flipagram.assetcopylib.AssetCopier;

/**
 * Created by kwatts on 11/6/17.
 */

public class Util {
    private static final String TAG = "MainActivity";

    public static int copyAssetsToCacheDirectory(Context ctx, boolean isDir, String file) {
        int count = 0;
        try {
            if (isDir) {
                File f = new File(ctx.getCacheDir().getAbsolutePath() + "/" + file);
                if (!f.exists()) {
                    f.mkdirs();
                    count = new AssetCopier(ctx).copy(file, f);
                }
            } else {
                count = new AssetCopier(ctx).copy(file, ctx.getCacheDir());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Toast.makeText(ctx, (count == -1 ? "There was an error copying" : "files copied " + count), Toast.LENGTH_LONG).show();
        return count;
    }


    public static int copyAssetsToFilesDirectory(Context ctx, boolean isDir, String file) {
        int count = 0;
        try {
            if (isDir) {
                File f = new File(ctx.getFilesDir().getAbsolutePath() + "/" + file);
                if (!f.exists()) {
                    f.mkdirs();
                    count = new AssetCopier(ctx).copy(file, f);
                }
            } else {
                count = new AssetCopier(ctx).copy(file, ctx.getFilesDir());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Toast.makeText(ctx, (count == -1 ? "There was an error copying" : "files copied " + count), Toast.LENGTH_LONG).show();
        return count;
    }

    static int parseColor(String c) {
        try {
            int skipInitial, skipBetween;
            if (c.charAt(0) == '#') {
                // #RGB, #RRGGBB, #RRRGGGBBB or #RRRRGGGGBBBB. Most significant bits.
                skipInitial = 1;
                skipBetween = 0;
            } else if (c.startsWith("rgb:")) {
                // rgb:<red>/<green>/<blue> where <red>, <green>, <blue> := h | hh | hhh | hhhh. Scaled.
                skipInitial = 4;
                skipBetween = 1;
            } else {
                return 0;
            }
            int charsForColors = c.length() - skipInitial - 2 * skipBetween;
            if (charsForColors % 3 != 0) return 0; // Unequal lengths.
            int componentLength = charsForColors / 3;
            double mult = 255 / (Math.pow(2, componentLength * 4) - 1);

            int currentPosition = skipInitial;
            String rString = c.substring(currentPosition, currentPosition + componentLength);
            currentPosition += componentLength + skipBetween;
            String gString = c.substring(currentPosition, currentPosition + componentLength);
            currentPosition += componentLength + skipBetween;
            String bString = c.substring(currentPosition, currentPosition + componentLength);

            int r = (int) (Integer.parseInt(rString, 16) * mult);
            int g = (int) (Integer.parseInt(gString, 16) * mult);
            int b = (int) (Integer.parseInt(bString, 16) * mult);
            return 0xFF << 24 | r << 16 | g << 8 | b;
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return 0;
        }
    }

    public static String[] getPermissions() {
        return new String[]{
                "-",
                "android.permission.WRITE_CONTACTS",
                "android.permission.GET_ACCOUNTS",
                "android.permission.READ_CONTACTS",
                "android.permission.ANSWER_PHONE_CALLS",
                "android.permission.READ_PHONE_NUMBERS",
                "android.permission.READ_PHONE_STATE",
                "android.permission.CALL_PHONE",
                "android.permission.ACCEPT_HANDOVER",
                "android.permission.USE_SIP",
                "android.permission.READ_CALENDAR",
                "android.permission.WRITE_CALENDAR",
                "android.permission.READ_CALL_LOG",
                "android.permission.WRITE_CALL_LOG",
                "android.permission.PROCESS_OUTGOING_CALLS",
                "android.permission.CAMERA",
                "android.permission.BODY_SENSORS",
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION",
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.RECORD_AUDIO",
                "android.permission.READ_SMS",
                "android.permission.RECEIVE_WAP_PUSH",
                "android.permission.RECEIVE_MMS",
                "android.permission.RECEIVE_SMS",
                "android.permission.SEND_SMS",
                "android.permission.READ_CELL_BROADCASTS"
        };
    }

}

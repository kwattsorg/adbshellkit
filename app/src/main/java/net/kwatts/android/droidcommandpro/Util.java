package net.kwatts.android.droidcommandpro;

import java.io.File;
import java.io.IOException;

import flipagram.assetcopylib.AssetCopier;

/**
 * Created by kwatts on 11/6/17.
 */

public class Util {
    private static final String TAG = "MainActivity";

    public static int copyAssetsToCacheDirectory(android.content.Context ctx, boolean isDir, String file) {
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
        //Toast.makeText(ctx, (count==-1 ? "There was an error copying" : "files copied " + count), Toast.LENGTH_LONG).show();
        return count;
    }

}

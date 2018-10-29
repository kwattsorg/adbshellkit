package net.kwatts.android.droidcommandpro;

/**
 * Created by kwatts on 2/22/18.
 */

import android.app.Application;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;
import com.topjohnwu.superuser.BusyBox;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ContainerApp;
import com.eggheadgames.aboutbox.AboutConfig;

/**
 * This is a subclass of {@link Application} used to provide shared objects for this app.
 */
public class App extends ContainerApp  {
    private static final String TAG = "Application";
    public static App INSTANCE = null;

    public App() {
        INSTANCE = this;
    }

    private static boolean isopen = true;


    @Override
    public synchronized void onCreate() {
        super.onCreate();
        Shell.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.verboseLogging(BuildConfig.DEBUG);
        // Use internal busybox
        BusyBox.setup(this);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Log.d(TAG, "onCreate");
        this.isopen = true;

        AboutConfig aboutConfig = AboutConfig.getInstance();
        aboutConfig.appName = getString(R.string.app_name);
        aboutConfig.appIcon = R.mipmap.ic_launcher;
        aboutConfig.version = "5.8";
        aboutConfig.author = "Kevin W";
        aboutConfig.aboutLabelTitle = "About App";
        aboutConfig.packageName = getApplicationContext().getPackageName();
        aboutConfig.buildType = AboutConfig.BuildType.GOOGLE;
        aboutConfig.emailAddress = "kwatkins@gmail.com";
        aboutConfig.emailSubject = "Feedback for " + aboutConfig.packageName;
        //aboutConfig.emailBody = "Hello...";

    }

}

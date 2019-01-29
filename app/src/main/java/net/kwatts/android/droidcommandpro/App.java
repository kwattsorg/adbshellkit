package net.kwatts.android.droidcommandpro;

/**
 * Created by kwatts on 2/22/18.
 */

import android.app.Application;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;
import com.topjohnwu.superuser.BusyBoxInstaller;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ContainerApp;
import com.eggheadgames.aboutbox.AboutConfig;

/**
 * This is a subclass of {@link Application} used to provide shared objects and superuser functionality across the full app
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

        android.util.TimingLogger timingLogger = new android.util.TimingLogger("droidcommander","App.create");
        //Shell.Config.addInitializers(BusyBoxInstaller.class);
        timingLogger.addSplit("Setting up Shell...");
        Shell.Config.setTimeout(20); //20 second timeout
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
        // Use internal busybox
       // BusyBox.setup(this);

        timingLogger.addSplit("Firebase.setPersistence()");
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Log.d(TAG, "onCreate");
        this.isopen = true;

        AboutConfig aboutConfig = AboutConfig.getInstance();
        aboutConfig.appName = getString(R.string.app_name);
        aboutConfig.appIcon = R.mipmap.ic_launcher;
        aboutConfig.version = BuildConfig.VERSION_NAME;
        aboutConfig.aboutLabelTitle = "About App";
        aboutConfig.extraTitle = "Description";
        aboutConfig.extra =
                "You now have access to the hidden system commands that came with your phone! <br><br>" +
                "<b> Hints to help guide your journey:</b><br>" +
                "<ul>" +
                "<li> Start with running my public commands available from the dropdown menu</li>" +
                "<li> Once you get the hang of it, login to create/edit/run your own private commands that automatically get saved to the cloud</li>" +
                "<li> Commands tagged <b>superuser</b> may require root</li>" +
                "<li> Commands tagged <b>pinned</b> get added to dynamic shortcuts (Android Nougat/7.1 or higher)</li>" +
                "<li> Ads are included to cover hosting costs, time, etc but feel free to turn them off in settings</li>" +
                "</ul>" +
                "<br>ADB Shellkit is fully open source! <a href=\"https://github.com/kwattsorg/adbshellkit\">https://github.com/kwattsorg/adbshellkit</a>" +
                " for feedback, support, or to dust off those coding skills :)";
        aboutConfig.packageName = getApplicationContext().getPackageName();
        aboutConfig.buildType = AboutConfig.BuildType.GOOGLE;
        aboutConfig.facebookUserName = null;
        aboutConfig.twitterUserName = null;
        aboutConfig.webHomePage = "https://github.com/kwattsorg/adbshellkit";
        aboutConfig.appPublisher = null;
        aboutConfig.companyHtmlPath = null;
        aboutConfig.privacyHtmlPath = "https://github.com/kwattsorg/adbshellkit/wiki/Privacy-Policy";
        aboutConfig.acknowledgmentHtmlPath = null;
        aboutConfig.emailAddress = "kwatkins@gmail.com";
        aboutConfig.emailSubject = "Feedback for " + aboutConfig.packageName;

        timingLogger.dumpToLog();


    }

}

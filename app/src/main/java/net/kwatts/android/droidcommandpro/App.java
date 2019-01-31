package net.kwatts.android.droidcommandpro;

/**
 * Created by kwatts on 2/22/18.
 */

import android.app.Application;
import android.util.Log;
import timber.log.Timber;
import com.google.firebase.database.FirebaseDatabase;
import com.topjohnwu.superuser.BusyBoxInstaller;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ContainerApp;
import com.eggheadgames.aboutbox.AboutConfig;
import com.crashlytics.android.Crashlytics;
import android.os.*;

import static timber.log.Timber.DebugTree;

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

        if (BuildConfig.DEBUG) {
            Timber.plant(new DebugTree());
        } else {
            Timber.plant(new ReleaseTree());
        }


        //android.util.TimingLogger timingLogger = new android.util.TimingLogger("droidcommander","App.create");
        //Shell.Config.addInitializers(BusyBoxInstaller.class);
        //timingLogger.addSplit("Setting up Shell...");
        Shell.Config.setTimeout(20); //20 second timeout
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
        // Use internal busybox
       // BusyBox.setup(this);

        //timingLogger.addSplit("Firebase.setPersistence()");
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

        //timingLogger.dumpToLog();


    }



    /** A tree which logs important information for crash reporting. */
    public static final class ReleaseTree extends Timber.Tree {
        @Override protected void log(int priority, String tag, String message, Throwable throwable) {
            switch (priority) {
                case Log.VERBOSE:
                case Log.DEBUG:
                case Log.INFO:
                    break;
                case Log.WARN:
                    logWarning(priority, tag, message);
                    break;
                case Log.ERROR:
                    logException(throwable);
                    break;
                default:
                    break;
            }
        }

        private void logWarning(int priority, String tag, String message) {
            Crashlytics.log(priority, tag, message);
        }

        private void logException(final Throwable throwable) {
            Crashlytics.logException(throwable);
        }
    }



    public static final class DebugTree extends Timber.DebugTree {
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            // Workaround for devices that doesn't show lower priority logs
            if (Build.MANUFACTURER.equals("HUAWEI") || Build.MANUFACTURER.equals("samsung")) {
                if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO)
                    priority = Log.ERROR;
            }
            super.log(priority, tag, message, t);
        }
        @Override
        protected String createStackElementTag(StackTraceElement element) {
            return String.format("net.kwatts.android.droidcommander [C:%s] [M:%s] [L:%s] ",
                    super.createStackElementTag(element),
                    element.getMethodName(),
                    element.getLineNumber());
        }
    }

}

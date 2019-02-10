package net.kwatts.android.droidcommandpro;

/**
 * Created by kwatts on 2/22/18.
 */

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import timber.log.Timber;
import com.google.firebase.database.FirebaseDatabase;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ContainerApp;
import com.eggheadgames.aboutbox.AboutConfig;
import com.crashlytics.android.Crashlytics;
import android.os.*;

import androidx.annotation.NonNull;

/**
 * This is a subclass of {@link Application} used to provide shared objects and superuser functionality across the full app
 */
public class App extends ContainerApp  {
    private static final String TAG = "Application";
    public static App INSTANCE = null;
    public App() {
        INSTANCE = this;
    }


    static {
        Shell.Config.setTimeout(20); //20 second timeout
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
        Shell.Config.addInitializers(ADBShellInitializer.class);
    }

    static class ADBShellInitializer extends Shell.Initializer {
        @Override
        public boolean onInit(Context context, @NonNull Shell shell) {
            SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
            if (p.getBoolean("includeToolsPath", true)) {
                String exportPath = "LD_LIBRARY_PATH=$LD_LIBRARY_PATH:" + App.INSTANCE.getCacheDir().getAbsolutePath() + "/lib" + ";" +
                                 "PATH=$PATH:" + App.INSTANCE.getCacheDir().getAbsolutePath() + "/scripts:" + App.INSTANCE.getCacheDir().getAbsolutePath() + "/bin" + ";";
                shell.newJob()
                        //.add(bashrc)                            /* Load a script from raw resources */
                        .add("export " + exportPath)   /* Run some commands */
                        .exec();
            }
            return true;
        }
    }

    @Override
    public synchronized void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new DebugTree());
        } else {
            Timber.plant(new ReleaseTree());
        }



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


        // Prepare local file and scripts, making them executable etc
        new Thread(new Runnable() {
            public void run() {
                int c1 = Util.copyAssetsToCacheDirectory(App.INSTANCE.getApplicationContext(),true,"bin");
                int c2 = Util.copyAssetsToCacheDirectory(App.INSTANCE.getApplicationContext(),true,"lib");
                int c3 = Util.copyAssetsToCacheDirectory(App.INSTANCE.getApplicationContext(),true,"scripts");
                int c4 = Util.copyAssetsToCacheDirectory(App.INSTANCE.getApplicationContext(),true,"share");
                Timber.d((c1 + c2 + c3 + c4) + " asset files copied!");


                String appCacheDir = getCacheDir().getAbsolutePath();


                Shell.sh("/system/bin/chmod -R 755 " + appCacheDir + "/bin "
                                + appCacheDir + "/lib " + appCacheDir + "/scripts "
                                + appCacheDir + "/share").exec();
                Timber.d("done setting asset permissions to executable!");


            }
        }).start();


        FirebaseDatabase.getInstance().setPersistenceEnabled(true);


    }


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

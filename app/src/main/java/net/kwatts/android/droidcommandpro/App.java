package net.kwatts.android.droidcommandpro;

/**
 * Created by kwatts on 2/22/18.
 */

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;

import flipagram.assetcopylib.AssetCopier;
import timber.log.Timber;
import com.google.firebase.database.FirebaseDatabase;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ContainerApp;
import com.eggheadgames.aboutbox.AboutConfig;
import com.crashlytics.android.Crashlytics;
import android.os.*;

import org.apache.commons.io.FileUtils;

import java.io.*;

import androidx.annotation.NonNull;

/**
 * This is a subclass of {@link Application} used to provide shared objects and superuser functionality across the full app
 */
public class App extends ContainerApp  {
    public static App INSTANCE = null;
    public App() {
        INSTANCE = this;
    }


    public static final String FILES_PATH = "/data/data/net.kwatts.android.droidcommandpro/files";


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

            InputStream bashrc;
            String exportPath;

            try {
                bashrc = new FileInputStream(App.FILES_PATH + "/home/bashrc");
            } catch (java.io.FileNotFoundException e) {
                bashrc = new ByteArrayInputStream("".getBytes());
            }


            if (p.getBoolean("includeToolsPath", true)) {
                exportPath = "export " +
                        "LD_LIBRARY_PATH=$LD_LIBRARY_PATH:" + App.FILES_PATH + "/lib" + ";" +
                        "PATH=$PATH:" + App.FILES_PATH + "/scripts:" + App.FILES_PATH + "/bin" + ";";
            } else {
                exportPath = "";
            }

            shell.newJob()
                    .add(bashrc)
                    .add(exportPath)
                    .exec();

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

                int count = 0;

                try {
                    int v1 = 0;
                    int v2 = BuildConfig.VERSION_CODE;

                    File verFile = new File(App.FILES_PATH + "/files_version.txt");

                    if (verFile.exists()) {
                        v1 = Integer.parseInt(FileUtils.readFileToString(verFile));
                    } else {
                        Timber.d(App.FILES_PATH + "/files_version.txt" + " doesn't exist, creating");
                    }

                    Timber.d("files_version.txt exists: " + verFile.exists());
                    Timber.d("files_version_code=" + v1 + ",app_version_code=" + v2);
                    Timber.d("buildconfig version_code: " + v2);

                    if (v1 != v2) {
                        count = new AssetCopier(App.INSTANCE.getApplicationContext())
                                .copy("files", App.INSTANCE.getApplicationContext().getFilesDir());
                        FileUtils.writeStringToFile(verFile, String.valueOf(BuildConfig.VERSION_CODE));
                    }

                } catch (Exception e) {
                    Timber.e(e);
                    e.printStackTrace();
                }

                Timber.d("Total files copied: " + count);
                Shell.sh("/system/bin/chmod -R 755 " + App.FILES_PATH + "/bin "
                            + App.FILES_PATH + "/lib " + App.FILES_PATH + "/scripts "
                            + App.FILES_PATH + "/share").exec();



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

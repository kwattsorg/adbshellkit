package net.kwatts.android.droidcommandpro.commands;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;


import com.topjohnwu.superuser.Shell;

import net.kwatts.android.droidcommandpro.ApiReceiver;
import net.kwatts.android.droidcommandpro.App;
import net.kwatts.android.droidcommandpro.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

//input text "adbshellkit app_risk_report --es application_name net.kwatts.android.things"
public class CommandAppRiskReport {
    public static int MINIMUM_APP_VERSION = 109;
    public static String cmd = "app_risk_report";
    public static String descr = "Runs a risk report for specified app";
    public static String args = "--es application_name <package name> --ez savetofile <true or false>";
    public static String[] permissions = { "" };

    // Volley/network requests
    //public static RequestQueue requestQueue;

    public static void onReceive(final ApiReceiver apiReceiver, final Context context, final Intent intent) {

        final String application_name = intent.getStringExtra("application_name");
        boolean saveFile = intent.getBooleanExtra("savetofile", false);



        ResultReturner.returnData(apiReceiver, intent, out -> {
            if (application_name == null) {
                out.print("");
            } else {
                JSONObject res = run(context,saveFile,application_name);

                out.print(res.toString(1));
            }
        });
    }


    public static JSONObject run(android.content.Context ctx, boolean saveFile, String appName) {

        JSONObject res = new JSONObject();

        String packageApkFileName = getApkFileName(ctx, appName);

        if (packageApkFileName == null) {
            return res;
        }

        List<String> stringsList = new ArrayList<>();
        List<String> stringHostList = new ArrayList<>();

        try {
            String cmd = "strings -n 6 " + packageApkFileName;
            try {
                String[] cmdline = {"sh", "-c", cmd};
                Process proc = Runtime.getRuntime().exec(cmdline);
                java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(proc.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    stringsList.add(line);
                }
            } catch (Exception e) {
                // Do nothing
            }

            Pattern p = Pattern.compile(Util.FQDN_REGEX);
            for (String s: stringsList) {
                Matcher m = p.matcher(s);
                if (m.matches()) {
                    stringHostList.add(s);
                }
            }
            res.put("app_package_name", appName);
            res.put("app_package_apk_filename", packageApkFileName);
            res.put("app_strings", stringsList.toArray());
            res.put("app_strings_hosts", stringHostList.toArray());

            //Find open services
            for (String h: stringHostList) {
                if (h.contains("appspot")) {
                    //TODO
                    // res.put ... getUrl("https://firebasestorage.googleapis.com/v0/b/things-73070.appspot.com/o/");
                }
            }

        } catch (Exception e) {
            Timber.e(e);
        }

        return res;

    }


    public static String getUrl(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
               // .header("Accept", "application/json")
               // .header("Content-Type", "application/json")
                .build();

        final String[] returnResult = new String[1];
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // e.printStackTrace();
                countDownLatch.countDown();
            }
            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    returnResult[0] = "";
                    //throw new IOException("Unexpected code " + response);
                } else {
                    returnResult[0] = response.body().string();
                    // do something wih the result
                }
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (Exception e) {
            Timber.e(e);
            return "";
        }

        return returnResult[0];
    }

    // Get from package manager or 'cmd package list packages -f'
    public static String getApkFileName(android.content.Context ctx, String appName) {
        final PackageManager pm = ctx.getPackageManager();
        List<ApplicationInfo> packages =  pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(appName)) {
                return packageInfo.sourceDir;
            }
        }

        return null;
    }

}

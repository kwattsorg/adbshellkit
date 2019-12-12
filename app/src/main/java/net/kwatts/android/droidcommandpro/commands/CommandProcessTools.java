package net.kwatts.android.droidcommandpro.commands;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import net.kwatts.android.droidcommandpro.ApiReceiver;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class CommandProcessTools {

    public static String cmd = "cmd_package_tools";

    public static String usage() {
        return "{\"cmd\":\"" + cmd + "\"," +
                "\"args\":\"cmd_process_tools --es kill <package> --es dump usage_stats\"}";

    }

    public static void onReceiveUsageStats(final ApiReceiver apiReceiver, final Context context, final Intent intent) {
        JSONObject res = new JSONObject();
        try {
            res.put("dump_usage_stats", getUsageStats(context));
        } catch (Exception e) {
            Timber.e("Exception trying to dump information");
        }
        ResultReturner.returnData(apiReceiver, intent, out -> {
            out.print(res.toString(1));
        });
    }

    public static void onReceiveDumpProcesses(final ApiReceiver apiReceiver, final Context context, final Intent intent) {
        JSONObject res = new JSONObject();
        try {
            res.put("dump_processes", getAllRunningProcesses(context));
        } catch (Exception e) {
            Timber.e("Exception trying to dump information");
        }
        ResultReturner.returnData(apiReceiver, intent, out -> {
            out.print(res.toString(1));
        });
    }

    public static void onReceiveDumpServices(final ApiReceiver apiReceiver, final Context context, final Intent intent) {
        JSONObject res = new JSONObject();
        try {
            res.put("dump_service", getAllRunningServices(context));
        } catch (Exception e) {
            Timber.e("Exception trying to dump information");
        }
        ResultReturner.returnData(apiReceiver, intent, out -> {
            out.print(res.toString(1));
        });
    }

    public static void onReceiveKillProcess(final ApiReceiver apiReceiver, final Context context, final Intent intent) {
        JSONObject res = new JSONObject();
        final String packageName = intent.hasExtra("kill") ? intent.getStringExtra("kill") : "";
        try {
            res.put("kill", killPackageProcesses(context, packageName));
        } catch (Exception e) {
            Timber.e("Exception trying to kill process for package");
        }

    }

    public static List<RunningAppProcessInfo> getPackageRunningProcesses(Context context, int packageUid) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        List<RunningAppProcessInfo> res = new ArrayList<>();
        for (RunningAppProcessInfo runningProInfo : procInfos) {
            if (runningProInfo.uid == packageUid) {
                res.add(runningProInfo);
            }
        }
        return res;
    }

    public static JSONObject getAllRunningProcesses(Context context) {
        JSONObject res = new JSONObject();

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> procInfos = am.getRunningAppProcesses();

        try {
            JSONArray resProcs = new JSONArray();
            for (RunningAppProcessInfo runningProInfo : procInfos) {
                JSONObject r = new JSONObject();
                try {
                    r.put("processName", runningProInfo.processName);
                    r.put("uid", runningProInfo.uid);
                    r.put("pid", runningProInfo.pid);
                    r.put("importance", runningProInfo.importance);
                    r.put("importanceReasonCode", runningProInfo.importanceReasonCode);
                    r.put("importanceReasonPid", runningProInfo.importanceReasonPid);
                    //r.put("importanceReasonComponent", runningProInfo.importanceReasonComponent.flattenToString());
                    r.put("lru", runningProInfo.lru);
                    r.put("lastTrimLevel", runningProInfo.lastTrimLevel);
                    r.put("describeContents", runningProInfo.describeContents());
                    r.put("pkgList", Arrays.toString(runningProInfo.pkgList));
                } catch (Exception je) {
                    r.put("exception", je.getMessage());
                }
                resProcs.put(r);
            }

            res.put("running_processes_size", procInfos.size());
            res.put("processes", resProcs);
        } catch (Exception e) {
            Timber.e("Exception getting processes");
        }


        return res;


    }

    // helpers

    public static JSONObject getAllRunningServices(Context context) {
        JSONObject res = new JSONObject();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = am.getRunningServices(Integer.MAX_VALUE);

        try {
            JSONArray resServices = new JSONArray();
            for (ActivityManager.RunningServiceInfo service : runningServices) {
                JSONObject r = new JSONObject();
                try {
                    r.put("activeSince", service.activeSince);
                    r.put("clientCount", service.clientCount);
                    r.put("clientLabel", service.clientLabel);
                    r.put("crashCount", service.crashCount);
                    r.put("flags", service.flags);
                    r.put("foreground", service.foreground);
                    r.put("lastActivityTime", service.lastActivityTime);
                    r.put("pid", service.pid);
                    r.put("restarting", service.restarting);
                    r.put("started", service.started);
                    r.put("uid", service.uid);
                    r.put("clientPackage", service.clientPackage);
                    r.put("process", service.process);
                    //might work...
                    r.put("describeContents", service.describeContents());
                    r.put("service.packageName", service.service.getPackageName());
                    r.put("service.className", service.service.getClassName());
                    PackageManager pm = context.getPackageManager();
                    r.put("appname", pm.getApplicationInfo(service.process, 0).loadLabel(pm).toString());
                } catch (Exception je) {
                    r.put("exception", je.getMessage());
                }
                resServices.put(r);
            }
            res.put("running_services_size", runningServices.size());
            res.put("services", resServices);
        } catch (Exception e) {
            Timber.e("Unable get process info");
        }

        return res;
    }

    public static JSONObject killPackageProcesses(Context context, String packageName) {
        JSONObject res = new JSONObject();
        int packageUid = getUidFromPackageName(context, packageName);
        List<RunningAppProcessInfo> appProcesses = getPackageRunningProcesses(context, packageUid);

        try {
            JSONArray resProcs = new JSONArray();

            for (RunningAppProcessInfo runningProInfo : appProcesses) {
                // only kill running processes and not the app
                if (runningProInfo.processName.equals(packageName)) {
                    continue;
                }

                JSONObject r = new JSONObject();
                try {
                    r.put("processName", runningProInfo.processName);
                    r.put("uid", runningProInfo.uid);
                    r.put("pid", runningProInfo.pid);
                    r.put("importance", runningProInfo.importance);
                    r.put("importanceReasonCode", runningProInfo.importanceReasonCode);
                    r.put("importanceReasonPid", runningProInfo.importanceReasonPid);
                    r.put("lru", runningProInfo.lru);
                    r.put("lastTrimLevel", runningProInfo.lastTrimLevel);
                    r.put("describeContents", runningProInfo.describeContents());
                    r.put("pkgList", Arrays.toString(runningProInfo.pkgList));

                    android.os.Process.killProcess(runningProInfo.pid);
                } catch (Exception pe) {
                    r.put("exception", pe.getMessage());
                }

                resProcs.put(r);
            }

            res.put("running_processes_size", appProcesses.size());
            res.put("killed_processes", resProcs);

        } catch (Exception e) {
            Timber.e("Unable get process info");
        }


        return res;
    }

    public static int getUidFromPackageName(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        int uid;

        try {
            ApplicationInfo info = pm.getApplicationInfo(context.getPackageName(), 0);
            return info.uid;
            //return pm.getNameForUid(uid);

        } catch (PackageManager.NameNotFoundException e) {
            uid = -1;
        }

        return uid;

    }

    public static String getAppNameByPID(Context context, int pid) {
        ActivityManager manager
                = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == pid) {
                return processInfo.processName;
            }
        }
        return "";
    }

    // https://stackoverflow.com/questions/30619349/android-5-1-1-and-above-getrunningappprocesses-returns-my-application-packag
    public static List<UsageStats> getActiveProcesses(Context context) {
        String mforegoundPppPackageName;

        Comparator<UsageStats> usageStatsComparator = new Comparator<UsageStats>() {
            @Override
            public int compare(UsageStats o1, UsageStats o2) {
                if (o1.getLastTimeUsed() > o2.getLastTimeUsed()) {
                    return 1;
                } else if (o1.getLastTimeUsed() < o2.getLastTimeUsed()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };


        UsageStatsManager usage = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();

        List<UsageStats> stats = usage.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0, time);
        Collections.sort(stats, usageStatsComparator);

        if (stats != null && stats.size() > 0) {
            if (stats.get(stats.size()).getPackageName().equals("android")) {
                stats.remove(stats.size());
            }
            mforegoundPppPackageName = stats.get(stats.size()).getPackageName();
        } else {
            mforegoundPppPackageName = "";
        }

        return stats;

    }

    // https://blog.usejournal.com/building-an-app-usage-tracker-in-android-fe79e959ab26
    public static JSONObject getUsageStats(Context context) {
        JSONObject res = new JSONObject();

        final long DAY = TimeUnit.DAYS.toMillis(1);

        try {
            JSONArray resStats = new JSONArray();

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

                long time = System.currentTimeMillis();
                List<UsageStats> appStatsList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - DAY * 128, time);
                for (UsageStats ustats : appStatsList) {
                    //if (appStatsList != null && !appStatsList.isEmpty()) {
                    JSONObject r = new JSONObject();
                    try {
                        r.put("describeContents", ustats.describeContents());
                        r.put("getFirstTimeStamp", ustats.getFirstTimeStamp());
                        r.put("getFirstTimeStampDate", new java.util.Date(ustats.getFirstTimeStamp()).toString());
                        r.put("getLastTimeStamp", ustats.getLastTimeStamp());
                        r.put("getLastTimeStampDate", new java.util.Date(ustats.getLastTimeStamp()).toString());
                        r.put("getLastTimeUsed", ustats.getLastTimeUsed());
                        r.put("getLastTimeUsedStampDate", new java.util.Date(ustats.getLastTimeUsed()).toString());
                        r.put("getPackageName", ustats.getPackageName());
                        r.put("getTotalTimeInForeground", ustats.getTotalTimeInForeground());
                        r.put("getTotalTimeInForegroundDate", new java.util.Date(ustats.getTotalTimeInForeground()).toString());
                    } catch (Exception pe) {
                        r.put("exception", pe.getMessage());
                    }
                    resStats.put(r);
                    //String currentApp = Collections.max(appStatsList, (o1, o2) -> Long.compare(o1.getLastTimeUsed(), o2.getLastTimeUsed())).getPackageName();
                }

                res.put("app_usage_stats_size", appStatsList.size());
                res.put("usage_stats", resStats);
            }

        } catch (Exception e) {
            Timber.e("Unable get process info");
        }


        return res;
    }

    public static boolean isSystemPackage(PackageInfo pkgInfo) {
        return (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    public String getCommandName() {
        return cmd;
    }

    public String[] getPermissions() {
        return new String[]{};
    }
}

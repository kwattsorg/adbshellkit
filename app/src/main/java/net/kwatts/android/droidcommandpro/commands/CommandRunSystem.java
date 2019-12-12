package net.kwatts.android.droidcommandpro.commands;

import android.content.Context;
import android.content.Intent;
import android.util.JsonWriter;

import net.kwatts.android.droidcommandpro.ApiReceiver;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CommandRunSystem {

    public static String cmd = "cmd_run_system";
    public static String[] permissions = {""};

    public static void onReceive(final ApiReceiver apiReceiver, final Context context, final Intent intent) {

        final String exec_command = intent.getStringExtra("exec_command");
        ResultReturner.returnData(context, intent, new ResultReturner.ResultJsonWriter() {
            public void writeJson(JsonWriter out) throws Exception {
                out.beginObject();
                if (exec_command == null) {
                    out.name("API_ERROR").value("No exec command given");
                } else {
                    try {
                        String[] cmdline = {"sh", "-c", exec_command};
                        Process proc = Runtime.getRuntime().exec(cmdline);
                        BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                        String line;
                        StringBuilder stdout = new StringBuilder();
                        while ((line = br.readLine()) != null)
                            stdout.append(line);

                        out.name("stdout").value(stdout.toString());
                    } catch (Exception ignored) {
                    }
                }

                out.endObject();
            }
        });
    }
}

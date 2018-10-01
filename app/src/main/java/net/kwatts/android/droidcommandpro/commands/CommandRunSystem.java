package net.kwatts.android.droidcommandpro.commands;

public class CommandRunSystem {

    public static String cmd = "%CMD_RUNSYSTEM%";

    public static ClassLoader execute(){
        android.util.Log.v("DroidCommandRunSystem", "called execute method of class " + CommandRunSystem.class.getName());
        try {


            String[] cmdline = {"sh", "-c", "settings list secure"};
            Process proc = Runtime.getRuntime().exec(cmdline);
            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = br.readLine()) != null)
                System.out.println(line);

            android.util.Log.v("DroidCommandRunSystem", CommandRunSystem.class.getName() + " output:" + line);

        } catch (Exception e) {}
        return CommandRunSystem.class.getClassLoader();
    }
}
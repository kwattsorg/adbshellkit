package net.kwatts.android.droidcommandpro.commands;

public class Engine {


    public static String processCommand(String coreCommand) {
        if (coreCommand.startsWith("%CMD_SMALI%")) {
            return "TODO: " + coreCommand;
        } else if (coreCommand.equals("[CODE] dalvik.system.DexClassLoader")) {
            return "TODO: " + coreCommand;
        }
        else {
            return null;
        }

    }
}

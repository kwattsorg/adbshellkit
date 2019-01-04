package net.kwatts.android.droidcommandpro.commands;

import com.google.common.collect.Lists;

import net.kwatts.android.droidcommandpro.App;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;
import timber.log.Timber;

public class Engine {


    public static String processCommand(Map<String,String> vars, String coreCommand) {

        List<String> cmdList = com.google.common.base.Splitter.on(' ').splitToList(coreCommand);
        if (cmdList.isEmpty() || cmdList.size() < 1) {
            Timber.d("Unable to process command for engines, is empty or < 1");
            return null;
        }

        String cmd = cmdList.get(0);

        //TODO: map vars to command arguments
        List<String> argList = cmdList.subList(1, cmdList.size());


        if (cmd.equalsIgnoreCase("cmd_smali")) {
            try {
                JSONObject res = CommandSmali.execute(App.INSTANCE.getApplicationContext(), argList);
                return res.toString(1);
            } catch (Exception e) {
                return "{\"exception\":\"" + e.getMessage() + "\"}";
            }
        } else if (cmd.equals("[CODE] dalvik.system.DexClassLoader")) {
            return "TODO: " + coreCommand;
        }
        else {
            return null;
        }

    }
}

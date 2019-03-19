package net.kwatts.android.droidcommandpro.commands;

import org.json.JSONObject;
import java.util.List;

//TODO: better way, https://github.com/termux/termux-api-package/blob/master/termux-api.c
// https://github.com/termux/termux-api/blob/master/app/src/main/java/com/termux/api/ShareAPI.java

public interface Command {
    JSONObject execute(android.content.Context ctx, List<String> args);
    String getCommandName();
    String[] getPermissions();
}

package net.kwatts.android.droidcommandpro.commands;

import org.json.JSONObject;
import java.util.List;

//TODO: better way, https://github.com/termux/termux-api-package/blob/master/termux-api.c

public interface Command {
    JSONObject execute(android.content.Context ctx, List<String> args);
    String getCommandName();
    String[] getPermissions();
}

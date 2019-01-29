package net.kwatts.android.droidcommandpro.commands;

import org.json.JSONObject;
import java.util.List;

public interface Command {
    JSONObject execute(android.content.Context ctx, List<String> args);
    String getCommandName();
    String[] getPermissions();
}

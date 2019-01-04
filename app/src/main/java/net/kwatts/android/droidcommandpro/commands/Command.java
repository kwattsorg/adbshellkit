package net.kwatts.android.droidcommandpro.commands;

import org.json.JSONObject;
import java.util.List;

public abstract class Command {
    public abstract JSONObject execute(android.content.Context ctx, List<String> args);

}

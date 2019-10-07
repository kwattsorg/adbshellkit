package net.kwatts.android.droidcommandpro.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kwatts on 2/15/18.
 */

public class User {

    public String username;
    public String email;
    public String phone_number;
    public String photo_url;
    public int permission;


    // 0 - ANONYMOUS
    //10 - USER
    //99 - ADMINISTRATOR

    public Map<String, String> vars = new HashMap<String, String>();

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, String phone_number, String photo_url) {
        this.username = username;
        this.email = email;
        this.phone_number = phone_number;
        this.photo_url = photo_url;
    }

    public String getVarValue(String key) {
        for (String name : vars.keySet()) {
            if (key == name) {
                return vars.get(key);
            }
        }
        return "";
    }

}
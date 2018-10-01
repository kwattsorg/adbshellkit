package net.kwatts.android.droidcommandpro.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kwatts on 4/30/18.
 */

public class GoogleUser {
    public String email;
    public String displayName;
    public String photoUrl;
    public String serverAuthCode;
    public String gId;
    public String gIdToken;
    public String oauthScopes;
    public long expirationTime;


    public Map<String, String> vars = new HashMap<String, String>();

    public GoogleUser() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }




    public String getVarValue(String key) {
        //for (Map.Entry<String,String> entry : vars.entrySet())
        //    System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
        for (String name : vars.keySet()) {
            if (key == name) {
                return vars.get(key);
            }
        }
        return "";
    }

}

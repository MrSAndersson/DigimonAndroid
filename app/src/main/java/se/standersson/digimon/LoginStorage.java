package se.standersson.digimon;

import android.app.Activity;
import android.content.SharedPreferences;
import java.util.HashMap;


public class LoginStorage {
    private SharedPreferences loginDetails;
    LoginStorage(Activity activity){
        loginDetails = activity.getPreferences(Activity.MODE_PRIVATE);
    }

    void setPreferences(HashMap<String, String> prefs){
        loginDetails.edit().putString("serverString", prefs.get("serverString")).commit();
        loginDetails.edit().putString("username", prefs.get("username")).commit();
        loginDetails.edit().putString("password", prefs.get("password")).commit();

    }

    HashMap<String, String> getPreferences(){
        HashMap<String, String> prefs = new HashMap<String, String>();
        prefs.put("serverString", loginDetails.getString("serverString", "test"));
        prefs.put("username", loginDetails.getString("username", "weee"));
        prefs.put("password", loginDetails.getString("password", "woo"));
        return prefs;
    }


}

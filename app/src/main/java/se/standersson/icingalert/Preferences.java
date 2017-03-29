package se.standersson.icingalert;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.google.firebase.messaging.FirebaseMessaging;

public class Preferences extends PreferenceActivity {

    private final SharedPreferences.OnSharedPreferenceChangeListener spChanged = new
            SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                                      String key) {
                    if (sharedPreferences.getBoolean("host_push", false)){
                        FirebaseMessaging.getInstance().subscribeToTopic("hosts");
                    } else {
                        FirebaseMessaging.getInstance().unsubscribeFromTopic("hosts");
                    }
                    if (sharedPreferences.getBoolean("service_push", false)){
                        FirebaseMessaging.getInstance().subscribeToTopic("services");
                    } else {
                        FirebaseMessaging.getInstance().unsubscribeFromTopic("services");
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(spChanged);
    }



    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

        }
    }



}

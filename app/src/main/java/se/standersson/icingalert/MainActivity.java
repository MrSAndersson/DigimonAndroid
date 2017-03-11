package se.standersson.icingalert;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.widget.Toolbar;
import com.google.firebase.messaging.FirebaseMessaging;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends Activity {
    private String reply;
    private static JSONObject data;
    private List<Host> hosts;
    private SwipeRefreshLayout swipeContainer;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("reply", reply);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                logOut();
                return true;
            case R.id.preferences:
                Intent prefIntent = new Intent(this, Preferences.class);
                startActivity(prefIntent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setActionBar(mainToolbar);

        // Subscribe to notifications according to saved settings
        SharedPreferences notificationPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (notificationPrefs.getBoolean("host_push", false)){
            FirebaseMessaging.getInstance().subscribeToTopic("hosts");
        }
        if (notificationPrefs.getBoolean("service_push", false)){
            FirebaseMessaging.getInstance().subscribeToTopic("services");
        }


        /*
         * Set up a callback for refresh PullDown
         */
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.exp_swipe);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() { refresh();}
        });

        Intent intent = getIntent();

        if (intent.getBooleanExtra("Notification", false)) {
            refresh();
        } else {

            // If we have a saved state, use that to create the list, otherwise, get from the intent
            if (savedInstanceState != null) {
                reply = savedInstanceState.getString("reply");
            } else {
                reply = intent.getStringExtra("reply");
            }

            try {
                data = new JSONObject(reply);
            } catch (JSONException e) {
                Toast.makeText(this, "Unable to parse JSON", Toast.LENGTH_LONG).show();
                logOut();
            }

            //Create expandableListView and fill with data
            ExpandableListView listView = (ExpandableListView) findViewById(R.id.main_expand_list);
            createExpandableListSummary();
            ExpandableListAdapter listAdapter = new mainExpandableListAdapter(this, hosts);
            listView.setAdapter(listAdapter);
        }
    }

    private void createExpandableListSummary() {
        /*
         * Prepare status info into
         */
        hosts = new ArrayList<>();
        HashMap<String, Integer> hostPositions = new HashMap<>();

        int services = 0, hostsDown = 0;

        // Check how many Hosts and Services are having trouble
        try {
            services = data.getJSONArray("services").length();
            hostsDown = data.getJSONArray("hosts").length();
        } catch (JSONException e) {
            Toast.makeText(this, "Couldn't find a Host/Services Array", Toast.LENGTH_LONG).show();
            logOut();
        }

        try {
            String hostName, serviceName;
            String serviceDetails;
            int state;


            /*
            * Add all downed hosts to the list first in order to sort them to the top
             */
            for (int x=0 ; x < hostsDown ; x++) {
                hostName = data.getJSONArray("hosts").getJSONObject(x).getJSONObject("attrs").getString("name");
                hosts.add(new Host(hostName, x));
                hostPositions.put(hostName, x);
            }

            /*
            * Loop through all services and store the hostname and the location of their respective failing services
             */
            for (int x=0 ; x < services ; x++) {
                hostName = data.getJSONArray("services").getJSONObject(x).getJSONObject("attrs").getString("host_name");
                serviceName = data.getJSONArray("services").getJSONObject(x).getJSONObject("attrs").getString("name");
                serviceDetails = data.getJSONArray("services").getJSONObject(x).getJSONObject("attrs").getJSONObject("last_check_result").getString("output");
                state = data.getJSONArray("services").getJSONObject(x).getJSONObject("attrs").getInt("state");

                if (hostPositions.containsKey(hostName)) {
                    hosts.get(hostPositions.get(hostName)).addService(x, serviceName, serviceDetails, state);
                } else {
                    hostPositions.put(hostName, hosts.size());
                    hosts.add(new Host(hostName));
                    hosts.get(hostPositions.get(hostName)).addService(x, serviceName, serviceDetails, state);
                }
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Problem with parsing hosts/services", Toast.LENGTH_LONG).show();
        }
    }

    private void logOut () {
        // Clear credentials and go back to login page
        SharedPreferences prefStorage = getSharedPreferences("Login", 0);
        prefStorage.edit().putString("serverString", "").apply();
        prefStorage.edit().putString("username", "").apply();
        prefStorage.edit().putString("password", "").apply();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void refresh() {

        //Get login credentials and make a call to get status data
        String[] prefsString = new String[3];
        SharedPreferences prefStorage = getSharedPreferences("Login", 0);
        prefsString[0] = prefStorage.getString("serverString", "");
        prefsString[1] = prefStorage.getString("username", "");
        prefsString[2] = prefStorage.getString("password", "");

        if (ServerInteraction.isConnected(getApplicationContext())){
            new refreshFetch().execute(prefsString);
        } else {
            Toast.makeText(getApplicationContext(), "No Network Connectivity", Toast.LENGTH_LONG).show();
        }

        swipeContainer.setRefreshing(false);
    }

    private class refreshFetch extends AsyncTask<String[], Integer, String> {
        /*
         * AsyncTask that does fetches the data outside of the UI thread and then resets the
         * expandableListView
         */
        @Override
        protected String doInBackground(String[]... data) {
            reply = ServerInteraction.fetchData(data[0]);
            return reply;
        }

        protected void onPostExecute(String reply){
            if (ServerInteraction.checkReply(getApplicationContext(), reply)) {
                try {
                    data = new JSONObject(reply);
                    ExpandableListView listView = (ExpandableListView) findViewById(R.id.main_expand_list);
                    createExpandableListSummary();
                    ExpandableListAdapter listAdapter = new mainExpandableListAdapter(getApplicationContext(), hosts);
                    listView.setAdapter(listAdapter);
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Unable to parse JSON", Toast.LENGTH_LONG).show();
                    logOut();
                }
            }
            swipeContainer.setRefreshing(false);
        }

    }

}

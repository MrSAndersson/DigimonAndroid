package se.standersson.digimon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.widget.Toolbar;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends Activity {

    private List<String> expandableListGroup;
    private HashMap<String, List<Integer>> hostServiceCounter;
    private List<String> hostsDowned;
    static JSONObject data;

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

        Intent intent = getIntent();

        /* Suppress the warning about Unchecked Cast since we know what we're doing
            Then, get the data from the indent.
         */
        @SuppressWarnings("unchecked")
        String reply = intent.getStringExtra("reply");
        Log.d("DataPayload", reply);
        try {
            data = new JSONObject(reply);
        } catch (JSONException e) {
            Toast.makeText(this, "Unable to parse JSON", Toast.LENGTH_LONG).show();
            logOut();
        }

        ExpandableListView listView = (ExpandableListView) findViewById(R.id.main_expand_list);
        createExpandableListSummary();
        //ExpandableListAdapter listAdapter = new mainExpandableListAdapter(this, dataMap, serviceList, expandableListGroup, hostServiceCounter, hostsDowned);
        ExpandableListAdapter listAdapter = new mainExpandableListAdapter(this, data, expandableListGroup, hostServiceCounter, hostsDowned);
        listView.setAdapter(listAdapter);
    }

    private void createExpandableListSummary() {
        expandableListGroup = new ArrayList<>();
        hostServiceCounter = new HashMap<>();
        hostsDowned = new ArrayList<>();

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
            String hostName;

            /*
            * Add all downed hosts to the list first
             */
            for (int x=0 ; x < hostsDown ; x++) {
                hostName = data.getJSONArray("hosts").getJSONObject(x).getJSONObject("attrs").getString("name");
                expandableListGroup.add(hostName);
                hostsDowned.add(hostName);
            }

            /*
            * Loop through all services and store the hostname and the location of their respective failing services
             */
            for (int x=0 ; x < services ; x++) {
                hostName = data.getJSONArray("services").getJSONObject(x).getJSONObject("attrs").getString("host_name");
                if (expandableListGroup.contains(hostName)) {
                    if (hostServiceCounter.containsKey(hostName)) {
                        hostServiceCounter.get(hostName).add(x);
                    } else {
                        hostServiceCounter.put(hostName, new ArrayList<Integer>());
                        hostServiceCounter.get(hostName).add(x);
                    }
                } else {
                    expandableListGroup.add(hostName);
                    hostServiceCounter.put(hostName, new ArrayList<Integer>());
                    hostServiceCounter.get(hostName).add(x);
                }
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Problem with parsing hosts/services", Toast.LENGTH_LONG).show();
        }
    }

    private void logOut () {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}

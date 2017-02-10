package se.standersson.digimon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    private List<String> expandableOldListGroup;
    private List<String> expandableListGroup;
    private HashMap<String, List<String>> listOldHashMap;
    private HashMap<String, List<String>> listContainer;

    static String reply;
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
        //createOldExpandableListData();
        //ExpandableListAdapter listAdapter = new mainExpandableListAdapter(this, expandableOldListGroup, listOldHashMap);
        ExpandableListAdapter listAdapter = new mainExpandableListAdapter(this, data);
        listView.setAdapter(listAdapter);
    }

    private void createExpandableListData() {
        expandableListGroup = new ArrayList<>();
        listContainer = new HashMap<>();
        int services = 0, hostsDown = 0;
        HashMap <String, int> hostSummary;

        try {
            services = data.getJSONArray("services").length();
            hostsDown = data.getJSONArray("hosts").length();
        } catch (JSONException e) {
            Toast.makeText(this, "Couldn't find a Host/Services Array", Toast.LENGTH_LONG).show();
            logOut();
        }
        try {
            for (int x=0 ; x < services ; x++) {
                if ()
            }
        } catch (Exception e) {

        }



    }

    /*private void createOldExpandableListData() {
        expandableOldListGroup = new ArrayList<>();
        listOldHashMap = new HashMap<>();

        expandableOldListGroup.add("grupp1an");
        expandableOldListGroup.add("Sfff");
        expandableOldListGroup.add("fddfdfddf");
        expandableOldListGroup.add("weee");

        List<String> grupp1 = new ArrayList<>();
        grupp1.add("This is Expandable ListView");

        List<String> gruppTvaan = new ArrayList<>();
        gruppTvaan.add("Expanded Listview");
        gruppTvaan.add("en grej");
        gruppTvaan.add("Ewww");

        List<String> ffdD = new ArrayList<>();
        ffdD.add("This is Expandable LifffstView");
        ffdD.add("Expanded Leeeeeistview");
        ffdD.add("en grefdfsfdj");
        ffdD.add("Ewwasasasdfw");

        List<String> wEEE = new ArrayList<>();
        wEEE.add("TWeee");
        wEEE.add("Expanded Leeeeeistview");
        listOldHashMap.put(expandableOldListGroup.get(0), grupp1);
        listOldHashMap.put(expandableOldListGroup.get(1), gruppTvaan);
        listOldHashMap.put(expandableOldListGroup.get(2), ffdD);
        listOldHashMap.put(expandableOldListGroup.get(3), wEEE);
    }*/

    private void logOut () {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}

package se.standersson.digimon;

import android.app.Activity;
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

    private ExpandableListView listView;
    private ExpandableListAdapter listAdapter;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listHashMap;

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
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
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

        listView = (ExpandableListView)findViewById(R.id.main_expand_list);
        initData();
        listAdapter = new expandableListAdapter(this, listDataHeader, listHashMap);
        listView.setAdapter(listAdapter);

        if (savedInstanceState == null) {
            //getFragmentManager().beginTransaction().add(R.id.activity_main, new MainListFragment()).commit();

        }

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
        }


    }

    private void initData() {
        listDataHeader = new ArrayList<>();
        listHashMap = new HashMap<>();

        listDataHeader.add("Grupp1");
        listDataHeader.add("Grupptvåan");
        listDataHeader.add("fddfdfddf");
        listDataHeader.add("weee");

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
    }

}

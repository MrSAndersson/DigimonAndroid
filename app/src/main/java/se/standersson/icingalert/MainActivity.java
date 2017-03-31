package se.standersson.icingalert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.firebase.messaging.FirebaseMessaging;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    public static List<Host> hosts;
    private FragmentPagerAdapter adapterViewPager;
    private int hostListCount;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save hosts list and hostListCount for when activity recreates
        outState.putSerializable("hosts", (Serializable) hosts);
        outState.putInt("hostListCount", hostListCount);
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Replace the current Intent with this one
        setIntent(intent);
        refresh(0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        // Subscribe to notifications according to saved settings
        SharedPreferences notificationPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (notificationPrefs.getBoolean("host_push", false)){
            FirebaseMessaging.getInstance().subscribeToTopic("hosts");
        }
        if (notificationPrefs.getBoolean("service_push", false)){
            FirebaseMessaging.getInstance().subscribeToTopic("services");
        }

        Intent intent = getIntent();


        // If we have a saved state, use that to create the list, otherwise, get from the intent
        if (savedInstanceState != null){
            // noinspection unchecked
            hosts = (List<Host>) savedInstanceState.getSerializable("hosts");
            hostListCount = savedInstanceState.getInt("hostListCount");
        } else {
            // noinspection unchecked
            hosts  = (List<Host>) intent.getSerializableExtra("hosts");
            hostListCount = intent.getIntExtra("hostListCount", 0);
        }

        ViewPager mainViewPager = (ViewPager) findViewById(R.id.main_view_pager);
        adapterViewPager = new MainPagerAdapter(getSupportFragmentManager(), hostListCount);
        mainViewPager.setAdapter(adapterViewPager);

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

    public void refresh(int position) {

        //Get login credentials and make a call to get status data
        String[] prefsString = new String[3];
        SharedPreferences prefStorage = getSharedPreferences("Login", 0);
        prefsString[0] = prefStorage.getString("serverString", "");
        prefsString[1] = prefStorage.getString("username", "");
        prefsString[2] = prefStorage.getString("password", "");

        switch (position){
            case 0:
                ((MainPagerAdapter)adapterViewPager).getFragment(1).setRefreshSpinner(true);
                break;
            case 1:
                ((MainPagerAdapter)adapterViewPager).getFragment(0).setRefreshSpinner(true);
                break;
        }

        if (ServerInteraction.isConnected(getApplicationContext())){
            new refreshFetch().execute(prefsString);
        } else {
            Toast.makeText(getApplicationContext(), "No Network Connectivity", Toast.LENGTH_LONG).show();
            ((MainPagerAdapter)adapterViewPager).getFragment(0).setRefreshSpinner(false);
            ((MainPagerAdapter)adapterViewPager).getFragment(1).setRefreshSpinner(false);
        }
    }

    private class refreshFetch extends AsyncTask<String[], Integer, String> {
        /*
         * AsyncTask that does fetches the data outside of the UI thread and then resets the
         * expandableListView
         */
        @Override
        protected String doInBackground(String[]... data) {
            try {
                return ServerInteraction.fetchData(data[0]);
            }catch (SocketTimeoutException e) {
                return "Connection Timed Out";
            } catch (MalformedURLException e) {
                return "Invalid URL";
            } catch (UnknownHostException e) {
                return "Resolve Failed";
            } catch (FileNotFoundException e) {
                return "FileNotFoundException";
            } catch (Exception e) {
                Log.e("NetworkException", e.toString());
                return "Unknown Exception";
            }
        }

        protected void onPostExecute(String replyString){
            if (ServerInteraction.checkReply(getApplicationContext(), replyString)) {
                try {
                    JSONObject data = new JSONObject(replyString);
                    Tools.createExpandableListSummary(data);
                    ((MainPagerAdapter) adapterViewPager).getFragment(0).update(hostListCount);
                    ((MainPagerAdapter) adapterViewPager).getFragment(1).update(hosts.size());
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Unable to parse response", Toast.LENGTH_LONG).show();
                    //logOut();
                }
            }
            ((MainPagerAdapter)adapterViewPager).getFragment(0).setRefreshSpinner(false);
            ((MainPagerAdapter)adapterViewPager).getFragment(1).setRefreshSpinner(false);

        }

    }

    private static class MainPagerAdapter extends FragmentPagerAdapter {
	    private static final int NUM_ITEMS = 2;
        private final int hostsDownNr;
        private final ProblemFragment[] fragmentArray = new ProblemFragment[2];

        MainPagerAdapter(android.support.v4.app.FragmentManager fragmentManager, Integer hostsCount) {
            super(fragmentManager);
            this.hostsDownNr = hostsCount;
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            switch (position) {
                case 0: // Trouble List
                    ProblemFragment troubleFragment = ProblemFragment.newInstance(position, hostsDownNr, true);
                    fragmentArray[0] = troubleFragment;
                    return troubleFragment;
                case 1: // All-things-list
                    ProblemFragment allFragment = ProblemFragment.newInstance(position, hosts.size(), false);
                    fragmentArray[1] = allFragment;
                    return allFragment;
                default:
                    return null;
            }
        }

        @Override
 public Object instantiateItem(ViewGroup container, int position) {
     ProblemFragment fragment = (ProblemFragment) super.instantiateItem(container, position);
     fragmentArray[position] = fragment;
     return fragment;
 }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Trouble";
                case 1:
                    return "All";
                default:
                    return "Wrong";
            }
        }

        private ProblemFragment getFragment(int position){
            return fragmentArray[position];
        }
    }

}

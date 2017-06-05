package se.standersson.icingalert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.firebase.messaging.FirebaseMessaging;
import org.json.JSONException;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;


public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private FragmentPagerAdapter adapterViewPager;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        final MenuItem searchItem = menu.findItem(R.id.main_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        ((MainPagerAdapter) adapterViewPager).getFragment(0).update(Tools.filterTextMatch(Tools.filterProblems(HostSingleton.getInstance().getHosts()), query));
        ((MainPagerAdapter) adapterViewPager).getFragment(1).update(Tools.filterTextMatch(HostSingleton.getInstance().getHosts(), query));
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query){
        ((MainPagerAdapter) adapterViewPager).getFragment(0).update(Tools.filterTextMatch(Tools.filterProblems(HostSingleton.getInstance().getHosts()), query));
        ((MainPagerAdapter) adapterViewPager).getFragment(1).update(Tools.filterTextMatch(HostSingleton.getInstance().getHosts(), query));
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
        setSupportActionBar(mainToolbar);

        // Subscribe to notifications according to saved settings
        SharedPreferences notificationPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (notificationPrefs.getBoolean("host_push", false)){
            FirebaseMessaging.getInstance().subscribeToTopic("hosts");
        }
        if (notificationPrefs.getBoolean("service_push", false)){
            FirebaseMessaging.getInstance().subscribeToTopic("services");
        }

        ViewPager mainViewPager = (ViewPager) findViewById(R.id.main_view_pager);
        adapterViewPager = new MainPagerAdapter(getSupportFragmentManager());
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

    public void refresh() {

        String[] prefsString = Tools.getLogin(this);

        ((MainPagerAdapter) adapterViewPager).getFragment(1).setRefreshSpinner(true);
        ((MainPagerAdapter) adapterViewPager).getFragment(0).setRefreshSpinner(true);

        if (Tools.isConnected(getApplicationContext())){
            new refreshFetch().execute(prefsString);
        } else {
            Toast.makeText(getApplicationContext(), "No Network Connectivity", Toast.LENGTH_LONG).show();
            ((MainPagerAdapter) adapterViewPager).getFragment(0).setRefreshSpinner(false);
            ((MainPagerAdapter) adapterViewPager).getFragment(1).setRefreshSpinner(false);
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
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Connection Timed Out", Toast.LENGTH_LONG).show();
                    }
                });
                return null;
            } catch (MalformedURLException e) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Invalid URL", Toast.LENGTH_LONG).show();
                    }
                });
                return null;
            } catch (UnknownHostException e) {
                runOnUiThread(new Runnable() {
                    public void run() {

                        Toast.makeText(getApplicationContext(), "Resolve Failed", Toast.LENGTH_LONG).show();
                    }
                });
                return null;
            } catch (FileNotFoundException e) {
                runOnUiThread(new Runnable() {
                    public void run() {

                        Toast.makeText(getApplicationContext(), "FileNotFoundException", Toast.LENGTH_LONG).show();
                    }
                });
                return null;
            } catch (Exception e) {
                Log.e("NetworkException", e.toString());
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Unable to Connect", Toast.LENGTH_LONG).show();
                    }
                });
                return null;
            }
        }

        protected void onPostExecute(String reply){
            if (reply != null) {
                try {
                    ServerInteraction.createExpandableListSummary(reply);
                    ((MainPagerAdapter) adapterViewPager).getFragment(0).update(Tools.filterProblems(HostSingleton.getInstance().getHosts()));
                    ((MainPagerAdapter) adapterViewPager).getFragment(1).update(HostSingleton.getInstance().getHosts());
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Unable to parse response", Toast.LENGTH_LONG).show();
                }
            }
            ((MainPagerAdapter)adapterViewPager).getFragment(0).setRefreshSpinner(false);
            ((MainPagerAdapter)adapterViewPager).getFragment(1).setRefreshSpinner(false);
        }

    }

    private static class MainPagerAdapter extends FragmentPagerAdapter {
        private static final int NUM_ITEMS = 2;
        private final HostListFragment[] fragmentArray = new HostListFragment[2];

        MainPagerAdapter(android.support.v4.app.FragmentManager fragmentManager) {
            super(fragmentManager);
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
                    HostListFragment troubleFragment = HostListFragment.newInstance(position, Tools.filterProblems(HostSingleton.getInstance().getHosts()));
                    fragmentArray[0] = troubleFragment;
                    return troubleFragment;
                case 1: // All-things-list
                    HostListFragment allFragment = HostListFragment.newInstance(position, HostSingleton.getInstance().getHosts());
                    fragmentArray[1] = allFragment;
                    return allFragment;
                default:
                    return null;
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            HostListFragment fragment = (HostListFragment) super.instantiateItem(container, position);
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

        private HostListFragment getFragment(int position){
            return fragmentArray[position];
        }
    }

}

package se.standersson.icingalert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import java.io.Serializable;
import java.util.List;

import se.standersson.icingalert.dummy.DummyContent;


public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, MainDataReceived, HostListFragment2.OnListFragmentInteractionListener {
    private MainPagerAdapter mainPagerAdapter;
    private SearchView searchView = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        searchView = (SearchView) menu.findItem(R.id.main_search).getActionView();
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {

        mainPagerAdapter.getFragment(0).update(Tools.filterTextMatch(Tools.filterProblems(HostSingleton.getInstance().getHosts()), query));
        mainPagerAdapter.getFragment(1).update(Tools.filterTextMatch(Tools.fullHostList(HostSingleton.getInstance().getHosts()), query));
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query){
        mainPagerAdapter.getFragment(0).update(Tools.filterTextMatch(Tools.filterProblems(HostSingleton.getInstance().getHosts()), query));
        mainPagerAdapter.getFragment(1).update(Tools.filterTextMatch(Tools.fullHostList(HostSingleton.getInstance().getHosts()), query));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                logOut(true);
                return true;
            case R.id.preferences:
                Intent prefIntent = new Intent(this, Preferences.class);
                startActivity(prefIntent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("hosts", (Serializable) HostSingleton.getInstance().getHosts());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Show progress bar until we've actually gotten the data.
        findViewById(R.id.main_progressbar).setVisibility(View.VISIBLE);
        findViewById(R.id.main_view_pager).setVisibility(View.GONE);


        Toolbar mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        // Subscribe to notifications according to saved settings
        SharedPreferences notificationPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (notificationPrefs.getBoolean("host_push", false)){
            FirebaseMessaging.getInstance().subscribeToTopic("hosts");
        }
        if (notificationPrefs.getBoolean("service_push", false)){
            FirebaseMessaging.getInstance().subscribeToTopic("services");
        }

        if (savedInstanceState == null) {
            // No previous data, get new instead
            new MainDataFetch(this).refresh(this);
        } else if (savedInstanceState.containsKey("hosts")) {
            // Data saved exists since before, use that and redraw UI with that

            //noinspection unchecked
            HostSingleton.getInstance().putHosts((List<Host>) savedInstanceState.getSerializable("hosts"));

            ViewPager mainViewPager = findViewById(R.id.main_view_pager);
            mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
            mainViewPager.setAdapter(mainPagerAdapter);

            // Hide progress bar and show main lists
            findViewById(R.id.main_progressbar).setVisibility(View.GONE);
            findViewById(R.id.main_view_pager).setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(getApplicationContext(), "Restoring state failed, please sign in again.", Toast.LENGTH_LONG).show();
            logOut(false);
        }
    }

    public MainPagerAdapter getMainPagerAdapter() {
        return mainPagerAdapter;
    }

    public SearchView getSearchView() {
        return searchView;
    }

    private void logOut (boolean clearCredentials) {

        if (clearCredentials) {
            SharedPreferences prefStorage = getSharedPreferences("Login", 0);
            prefStorage.edit().putString("serverString", "").apply();
            prefStorage.edit().putString("username", "").apply();
            prefStorage.edit().putString("password", "").apply();
            FirebaseMessaging.getInstance().unsubscribeFromTopic("hosts");
            FirebaseMessaging.getInstance().unsubscribeFromTopic("services");
        }

        // Go back to login page

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void mainDataReceived(boolean success) {
        if (success) {
            ViewPager mainViewPager = findViewById(R.id.main_view_pager);
            mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
            mainViewPager.setAdapter(mainPagerAdapter);

            // Hide progress bar and show main lists
            findViewById(R.id.main_progressbar).setVisibility(View.GONE);
            findViewById(R.id.main_view_pager).setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get Data", Toast.LENGTH_LONG).show();
            logOut(false);
        }
    }

    @Override
    public void onListFragmentInteraction(HostList host) {

    }

}

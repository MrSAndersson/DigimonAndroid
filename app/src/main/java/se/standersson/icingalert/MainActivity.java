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


import com.google.firebase.messaging.FirebaseMessaging;


public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
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

        mainPagerAdapter.getFragment(0).update(Tools.filterTextMatch(Tools.filterProblems(HostSingleton.getInstance().getHosts()), query), true);
        mainPagerAdapter.getFragment(1).update(Tools.filterTextMatch(Tools.fullHostList(HostSingleton.getInstance().getHosts()), query), true);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query){
        mainPagerAdapter.getFragment(0).update(Tools.filterTextMatch(Tools.filterProblems(HostSingleton.getInstance().getHosts()), query), true);
        mainPagerAdapter.getFragment(1).update(Tools.filterTextMatch(Tools.fullHostList(HostSingleton.getInstance().getHosts()), query), true);
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

        ViewPager mainViewPager = findViewById(R.id.main_view_pager);
        mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        mainViewPager.setAdapter(mainPagerAdapter);

    }

    public MainPagerAdapter getMainPagerAdapter() {
        return mainPagerAdapter;
    }

    public SearchView getSearchView() {
        return searchView;
    }

    private void logOut () {
        // Clear credentials and go back to login page
        SharedPreferences prefStorage = getSharedPreferences("Login", 0);
        prefStorage.edit().putString("serverString", "").apply();
        prefStorage.edit().putString("username", "").apply();
        prefStorage.edit().putString("password", "").apply();
        FirebaseMessaging.getInstance().unsubscribeFromTopic("hosts");
        FirebaseMessaging.getInstance().unsubscribeFromTopic("services");
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}

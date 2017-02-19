package se.standersson.digimon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.ICUUncheckedIOException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;


public class LoginActivity extends Activity {

    TextView showPrefs;
    private static Handler handler;
    String[] prefsString = new String[3];
    View serverView;
    View usernameView;
    View passwordView;
    View loginButton;
    View savedButton;
    View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        handler = new Handler();
        serverView = findViewById(R.id.login_server);
        usernameView = findViewById(R.id.login_username);
        passwordView = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        //savedButton = findViewById(R.id.saved_button);
        progressBar = findViewById(R.id.login_progressbar);
        savedLogin();
    }


    void logIn(View view){
        EditText editTextServer = (EditText) findViewById(R.id.login_server);
        EditText editTextUsername = (EditText) findViewById(R.id.login_username);
        EditText editTextPassword = (EditText) findViewById(R.id.login_password);

        prefsString[0] = editTextServer.getText().toString();
        prefsString[1] = editTextUsername.getText().toString();
        prefsString[2] = editTextPassword.getText().toString();

        HashMap<String, String> prefs = new HashMap<>();
        prefs.put("serverString", prefsString[0]);
        prefs.put("username", prefsString[1]);
        prefs.put("password", prefsString[2]);

        /*
        Store the settings, get data and start the MainActivity
         */
        //new LoginStorage(this).setLoginDetails(prefs);
        SharedPreferences prefStorage = getSharedPreferences("Login", 0);
        prefStorage.edit().putString("serverString", prefsString[0]).apply();
        prefStorage.edit().putString("username", prefsString[1]).apply();
        prefStorage.edit().putString("password", prefsString[2]).apply();



        if (isConnected()){
            progressBar.setVisibility(View.VISIBLE);
            new loginFetch().execute(prefsString);
        }
        //updateData(this, prefs, false);
    }

    void savedLogin(){
        /*
        A method for testing the storage.
         */
        //HashMap<String, String> prefs = new LoginStorage(this).getLoginDetails();
        SharedPreferences prefStorage = getSharedPreferences("Login", 0);
        HashMap<String, String> prefs = new HashMap<>();
        prefsString[0] = prefStorage.getString("serverString", "");
        prefsString[1] = prefStorage.getString("username", "");
        prefsString[2] = prefStorage.getString("password", "");
        prefs.put("serverString", prefsString[0]);
        prefs.put("username", prefsString[1]);
        prefs.put("password", prefsString[2]);

        prefsString[0] = prefs.get("serverString");
        prefsString[1] = prefs.get("username");
        prefsString[2] = prefs.get("password");
        /*
        Login with the saved credentials
         */
        if (prefs.get("serverString").equals("") && prefs.get("username").equals("") && prefs.get("password").equals("")) {
            Toast.makeText(this, "No Saved Credentials", Toast.LENGTH_LONG).show();
        } else {
            if (isConnected()){
                progressBar.setVisibility(View.VISIBLE);

                new loginFetch().execute(prefsString);
            }
        }

    }

    private class loginFetch extends AsyncTask<String[], Integer, String> {
        @Override
        protected String doInBackground(String[]... data) {

            return ServerInteraction.fetchData(data[0]);
        }

        protected void onPostExecute(String reply){
            if (ServerInteraction.checkReply(getApplicationContext(), reply)) {
                startMainActivity(reply);
            }
            progressBar.setVisibility(View.GONE);
        }

    }

    void startMainActivity(String reply){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("reply", reply);
        startActivity(intent);

        finish();
    }

    boolean isConnected(){
        /*
        * Check Network Connectivity and then request data from Icinga
        * */

        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}


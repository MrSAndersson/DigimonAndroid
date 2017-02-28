package se.standersson.digimon;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class LoginActivity extends Activity {
    private static final int MY_PERMISSIONS_REQUEST_INTERNET = 1;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE = 2;
    String[] prefsString = new String[3];
    View serverView;
    View usernameView;
    View passwordView;
    View loginButton;
    View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        serverView = findViewById(R.id.login_server);
        usernameView = findViewById(R.id.login_username);
        passwordView = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        progressBar = findViewById(R.id.login_progressbar);
        checkConnectivityPermissions();
        //savedLogin();
    }


    public void logIn(View view){
        EditText editTextServer = (EditText) findViewById(R.id.login_server);
        EditText editTextUsername = (EditText) findViewById(R.id.login_username);
        EditText editTextPassword = (EditText) findViewById(R.id.login_password);

        prefsString[0] = editTextServer.getText().toString();
        prefsString[1] = editTextUsername.getText().toString();
        prefsString[2] = editTextPassword.getText().toString();


        /*
        Store the settings, get data and start the MainActivity
         */
        SharedPreferences prefStorage = getSharedPreferences("Login", 0);
        prefStorage.edit().putString("serverString", prefsString[0]).apply();
        prefStorage.edit().putString("username", prefsString[1]).apply();
        prefStorage.edit().putString("password", prefsString[2]).apply();



        if (ServerInteraction.isConnected(getApplicationContext())){
            progressBar.setVisibility(View.VISIBLE);
            new loginFetch().execute(prefsString);
        }
    }

    void savedLogin(){
        /*
        A method for logging in with saved credentials
         */
        SharedPreferences prefStorage = getSharedPreferences("Login", 0);
        prefsString[0] = prefStorage.getString("serverString", "");
        prefsString[1] = prefStorage.getString("username", "");
        prefsString[2] = prefStorage.getString("password", "");

        /*
        Login with the saved credentials if some are saved
         */
        if (!(prefsString[0].equals("") && prefsString[1].equals("") && prefsString[2].equals(""))) {
            if (ServerInteraction.isConnected(getApplicationContext())){
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


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkInternetPermissions();
                }
            }
            case MY_PERMISSIONS_REQUEST_INTERNET: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    savedLogin();
                }
            }
        }
    }

    void checkInternetPermissions(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    MY_PERMISSIONS_REQUEST_INTERNET);
        }
        savedLogin();
    }
    void checkConnectivityPermissions(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                    MY_PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE);
        }
        checkInternetPermissions();
    }

}


package se.standersson.icingalert;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;


public class LoginActivity extends AppCompatActivity {
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

        // Get stored credentials

        SharedPreferences prefStorage = getSharedPreferences("Login", 0);
        prefsString[0] = prefStorage.getString("serverString", "");
        prefsString[1] = prefStorage.getString("username", "");
        prefsString[2] = prefStorage.getString("password", "");

        if (ServerInteraction.isConnected(getApplicationContext())){
            if (!(prefsString[0].equals("") && prefsString[1].equals("") && prefsString[2].equals(""))) {
                progressBar.setVisibility(View.VISIBLE);
                startLogin();
            } else {
                showLoginUI(true);
            }
        } else {
            showLoginUI(true);
        }
    }


    public void logInButton(View view){

        // Get the settings from the text boxes
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

        // If we have internet connectivity, start the connection to the server
        if (ServerInteraction.isConnected(getApplicationContext())){
            progressBar.setVisibility(View.VISIBLE);
            startLogin();
        }
    }

   /*
    *  Checks for Internet Permissions
    */
    void startLogin(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, MY_PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE);
        } else {
            checkInternetPermissions();
        }
    }

    void checkInternetPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, MY_PERMISSIONS_REQUEST_INTERNET);
        } else {
            new sendRequest().execute(prefsString);
        }
    }

    // Callback for Internet permission checks
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],@NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkInternetPermissions();
                } else {
                    Toast.makeText(this, "No Network State access given", Toast.LENGTH_LONG).show();
                    showLoginUI(true);
                }
            }
            case MY_PERMISSIONS_REQUEST_INTERNET: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new sendRequest().execute(prefsString);
                } else {
                    Toast.makeText(this, "No Internet Permissions given", Toast.LENGTH_LONG).show();
                    showLoginUI(true);
                }
            }
        }
    }

    private class sendRequest extends AsyncTask<String[], Integer, String> {
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

        protected void onPostExecute(String reply){
            if (ServerInteraction.checkReply(getApplicationContext(), reply)) {
                startMainActivity(reply);
            } else {
                progressBar.setVisibility(View.GONE);
                showLoginUI(true);
            }
        }
    }

    void startMainActivity(String reply){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("reply", reply);
        startActivity(intent);

        finish();
    }

    void showLoginUI(boolean show) {
        if (show) {
            serverView.setVisibility(View.VISIBLE);
            usernameView.setVisibility(View.VISIBLE);
            passwordView.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.VISIBLE);
        } else {
            serverView.setVisibility(View.GONE);
            usernameView.setVisibility(View.GONE);
            passwordView.setVisibility(View.GONE);
            loginButton.setVisibility(View.GONE);
        }
    }
}


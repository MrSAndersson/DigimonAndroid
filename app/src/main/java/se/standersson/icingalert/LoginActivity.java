package se.standersson.icingalert;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_INTERNET = 1;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE = 2;
    private final String[] prefsString = new String[3];
    private View logoView;
    private View serverView;
    private View usernameView;
    private View passwordView;
    private View loginButton;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        logoView = findViewById(R.id.login_logo);
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

        if (Tools.isConnected(getApplicationContext())){
            if (!(prefsString[0].equals("") && prefsString[1].equals("") && prefsString[2].equals(""))) {
                progressBar.setVisibility(View.VISIBLE);
                startLogin();
            } else {
                showLoginUI();
            }
        } else {
            showLoginUI();
        }
    }


    public void logInButton(@SuppressWarnings("UnusedParameters") View view){

        // Get the settings from the text boxes
        EditText editTextServer = findViewById(R.id.login_server);
        EditText editTextUsername = findViewById(R.id.login_username);
        EditText editTextPassword = findViewById(R.id.login_password);

        prefsString[0] = editTextServer.getText().toString();
        prefsString[1] = editTextUsername.getText().toString();
        prefsString[2] = editTextPassword.getText().toString();

        // If we have internet connectivity, start the connection to the server
        if (Tools.isConnected(getApplicationContext())){
            progressBar.setVisibility(View.VISIBLE);
            startLogin();
        }
    }

   /*
    *  Checks for Internet Permissions
    */
    private void startLogin(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, MY_PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE);
        } else {
            checkInternetPermissions();
        }
    }

    private void checkInternetPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, MY_PERMISSIONS_REQUEST_INTERNET);
        } else {
            sendRequest();
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
                    showLoginUI();
                }
            }
            case MY_PERMISSIONS_REQUEST_INTERNET: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendRequest();
                } else {
                    Toast.makeText(this, "No Internet Permissions given", Toast.LENGTH_LONG).show();
                    showLoginUI();
                }
            }
        }
    }

    private void sendRequest(){
        if (Tools.isConnected(this)) {

            VolleySingleton.getInstance(this).getRequestQueue();

            final String requestString = prefsString[0] + "/v1";

            JsonObjectRequest loginTestRequest = new JsonObjectRequest(Request.Method.GET, requestString, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                     /*
                        Store the login details
                     */
                    SharedPreferences prefStorage = getSharedPreferences("Login", 0);
                    prefStorage.edit().putString("serverString", prefsString[0]).apply();
                    prefStorage.edit().putString("username", prefsString[1]).apply();
                    prefStorage.edit().putString("password", prefsString[2]).apply();

                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    startActivity(intent);

                    finish();
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                    // Show login UI
                    progressBar.setVisibility(View.GONE);
                    showLoginUI();

                    // Handle various kinds of Network errors
                    if (error instanceof TimeoutError) {
                        Toast.makeText(getBaseContext(),"Connection Timeout", Toast.LENGTH_LONG).show();
                    } else if (error instanceof NoConnectionError) {
                        Toast.makeText(getBaseContext(),"Connection Error", Toast.LENGTH_LONG).show();
                    } else if (error instanceof AuthFailureError) {
                        Toast.makeText(getBaseContext(),"Wrong Credentials", Toast.LENGTH_LONG).show();
                    } else if (error instanceof ServerError) {
                        Toast.makeText(getBaseContext(),"Server Error, check Server String", Toast.LENGTH_LONG).show();
                    } else if (error instanceof NetworkError) {
                        Toast.makeText(getBaseContext(),"Network Error", Toast.LENGTH_LONG).show();
                    } else if (error instanceof ParseError) {
                        Toast.makeText(getBaseContext(),"Could not parse response", Toast.LENGTH_LONG).show();
                    } else if (error.getCause().getCause() instanceof MalformedURLException) {
                        Toast.makeText(getBaseContext(), "Malformed URL", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getBaseContext(),"Could not get data", Toast.LENGTH_LONG).show();
                    }
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    String credentials = String.format("Basic %s", Base64.encodeToString(String.format("%s:%s", prefsString[1], prefsString[2]).getBytes(), Base64.DEFAULT));
                    params.put("Authorization", credentials);
                    params.put("Accept", "application/json");
                    return params;
                }
            };

            VolleySingleton.getInstance(this).addToRequestQueue(loginTestRequest);
        } else {
            Toast.makeText(this, R.string.no_connectivity, Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoginUI() {
        logoView.setVisibility(View.VISIBLE);
        serverView.setVisibility(View.VISIBLE);
        usernameView.setVisibility(View.VISIBLE);
        passwordView.setVisibility(View.VISIBLE);
        loginButton.setVisibility(View.VISIBLE);

    }
}


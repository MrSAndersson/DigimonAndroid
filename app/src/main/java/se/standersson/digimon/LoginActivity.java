package se.standersson.digimon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
        savedButton = findViewById(R.id.saved_button);
        progressBar = findViewById(R.id.login_progressbar);
    }


    void logIn(View view){
        EditText editTextServer = (EditText) findViewById(R.id.login_server);
        EditText editTextUsername = (EditText) findViewById(R.id.login_username);
        EditText editTextPassword = (EditText) findViewById(R.id.login_password);

        String server = editTextServer.getText().toString();
        String username = editTextUsername.getText().toString();
        String password = editTextPassword.getText().toString();

        HashMap<String, String> prefs = new HashMap<>();
        prefs.put("serverString", server);
        prefs.put("username", username);
        prefs.put("password", password);

        /*
        Store the settings, get data and start the MainActivity
         */
        new LoginStorage(this).setLoginDetails(prefs);
        updateData(this, prefs, false);
    }

    void savedLogin(View view){
        /*
        A method for testing the storage.
         */
        HashMap<String, String> prefs = new LoginStorage(this).getLoginDetails();
        String outputString = "Server: " + prefs.get("serverString") + "\n" + "Username: " + prefs.get("username") + "\n" + "Password: " + prefs.get("password");
        /*
        Login with the saved credentials
         */
        if (prefs.get("serverString").equals("") && prefs.get("username").equals("") && prefs.get("password").equals("")) {
            Toast.makeText(this, "No Saved Credentials", Toast.LENGTH_LONG).show();
        } else {
            updateData(this, prefs, false);
        }

    }

    void updateData(final Context context, final HashMap<String, String> prefs, final boolean refresh) {

        /*
        * Check Network Connectivity and then request data from Icinga
        * */
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {


            progressBar.setVisibility(View.VISIBLE);
            new Thread() {
                public void run() {

                    final String reply = ServerInteraction.fetchData(prefs);
                    handler.post(new Runnable() {
                        public void run() {
                            if (ServerInteraction.checkReply(context, reply)) {
                                Intent intent = new Intent(context, MainActivity.class);
                                intent.putExtra("reply", reply);
                                startActivity(intent);

                                finish();
                            }
                        }
                    });

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }.start();
        } else {
            Toast.makeText(this, "No Internet Connectivity", Toast.LENGTH_LONG).show();
        }

    }





}


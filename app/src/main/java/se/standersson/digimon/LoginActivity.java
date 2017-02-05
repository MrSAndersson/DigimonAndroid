package se.standersson.digimon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        handler = new Handler();
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
        updateData(this, prefs);
    }

    void savedLogin(View view){
        /*
        A method for testing the storage.
         */
        showPrefs = (TextView)findViewById(R.id.show_prefs);
        HashMap<String, String> prefs = new LoginStorage(this).getLoginDetails();
        String outputString = "Server: " + prefs.get("serverString") + "\n" + "Username: " + prefs.get("username") + "\n" + "Password: " + prefs.get("password");
        /*
        Login with the saved credentials
         */
        if (prefs.get("serverString").equals("") && prefs.get("username").equals("") && prefs.get("password").equals("")) {
            Toast.makeText(this, "No Saved Credentials", Toast.LENGTH_LONG).show();
        } else {
            updateData(this, prefs);
        }

    }

    void updateData(final Context context, final HashMap<String, String> prefs) {

        new Thread() {
            public void run() {
                final String reply = IcingaInteraction.fetchData(prefs);
                handler.post(new Runnable(){
                    public void run(){
                        switch (reply) {
                            case "Wrong credentials\n":
                                Toast.makeText(context, "Wrong Credentials", Toast.LENGTH_LONG).show();
                                return;
                            case "Invalid URL":
                                Toast.makeText(context, reply, Toast.LENGTH_LONG).show();
                                return;
                            case "Resolve Failed":
                                Toast.makeText(context, reply, Toast.LENGTH_LONG).show();
                                return;
                            case "FileNotFoundException":
                                Toast.makeText(context, reply, Toast.LENGTH_LONG).show();
                                return;
                            case "Unknown Exception":
                                Toast.makeText(context, reply, Toast.LENGTH_LONG).show();
                                return;
                        }
                        Intent intent = new Intent(context,MainActivity.class);
                        intent.putExtra("reply", reply);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        }.start();
    }

}


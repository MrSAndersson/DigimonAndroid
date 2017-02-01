package se.standersson.digimon;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import java.util.HashMap;


public class LoginActivity extends Activity {

    TextView showPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
    }


    public void logIn(View view){
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

        new LoginStorage(this).setPreferences(prefs);
    }

    public void showPrefs(View view){
        showPrefs = (TextView)findViewById(R.id.show_prefs);
        HashMap<String, String> prefs = new LoginStorage(this).getPreferences();
        String outputString = "Server: " + prefs.get("serverString") + "\n" + "Username: " + prefs.get("username") + "\n" + "Password: " + prefs.get("password");
        showPrefs.setText(outputString);
    }
}


package se.standersson.digimon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity {

    static String reply;
    static JSONObject data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setActionBar(mainToolbar);

        Intent intent = getIntent();

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.activity_main, new MainListFragment()).commit();

        }

        /* Suppress the warning about Unchecked Cast since we know what we're doing
            Then, get the data from the indent.
         */
        @SuppressWarnings("unchecked")
        String reply = intent.getStringExtra("reply");
        try {
            data = new JSONObject(reply);
        } catch (JSONException e) {
            Toast.makeText(this, "Unable to parse JSON", Toast.LENGTH_LONG).show();
        }
    }
}

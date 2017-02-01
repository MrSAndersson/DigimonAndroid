package se.standersson.digimon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.util.HashMap;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        /* Suppress the warning about Unchecked Cast since we know what we're doing
            Then, get the preferences from the intent.
         */
        @SuppressWarnings("unchecked")
        HashMap<String, String> prefs = (HashMap<String, String>)intent.getSerializableExtra("prefs");
    }
}

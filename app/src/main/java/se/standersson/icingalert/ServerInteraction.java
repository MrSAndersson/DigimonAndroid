package se.standersson.icingalert;

import android.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;



class ServerInteraction {



    static String fetchData(final String[] prefs)throws  Exception{

        /*
        * Try Catch to catch all errors in network communication
        * */
        JSONObject replyGroup = new JSONObject();
        String serverString = prefs[0];
        String username = prefs[1];
        String password = prefs[2];
        String statusURL = serverString + "/v1/status/CIB";
        String hostURL = serverString + "/v1/objects/hosts?attrs=last_check_result&attrs=state&attrs=name&attrs=acknowledgement";
        String serviceURL = serverString + "/v1/objects/services?attrs=last_check_result&attrs=state&attrs=name&attrs=host_name&attrs=last_state&attrs=last_state_change&attrs=enable_notifications&attrs=acknowledgement";
        String commentsURL = serverString + "/v1/objects/comments?attrs=author&attrs=host_name&attrs=service_name&attrs=text";

            /*
            * Create a connection with input/output with a plaintext body
            * */
        String credentials = "Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP);

            replyGroup = sendStatusRequest(replyGroup, "status", statusURL, credentials);
            replyGroup = sendStatusRequest(replyGroup, "hosts", hostURL, credentials);
            replyGroup = sendStatusRequest(replyGroup, "services", serviceURL, credentials);
            replyGroup = sendStatusRequest(replyGroup, "comments", commentsURL, credentials);

        return replyGroup.toString();

    }

    private static JSONObject sendStatusRequest(JSONObject completeObject, String part, String serverString, String credentials) throws Exception {
        URL url = new URL(serverString);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", credentials);
        connection.setRequestProperty("Content-Type", "text/plain");
        connection.setConnectTimeout(10000);
        connection.setDoInput(true);
        connection.setUseCaches(false);


        // Create the BufferedReader and add all batches together
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String json = "";
        String tmp;

        while ((tmp = reader.readLine()) != null) {
            json += tmp + "\n";
        }
        reader.close();
        connection.disconnect();
        if (part.equals("status")) {
            completeObject.put(part, new JSONObject(json).getJSONArray("results").getJSONObject(0).getJSONObject("status"));
        } else {
            completeObject.put(part, new JSONObject(json).getJSONArray("results"));
        }
        return completeObject;
    }
}

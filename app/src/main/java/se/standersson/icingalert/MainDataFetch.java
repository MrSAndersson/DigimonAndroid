package se.standersson.icingalert;

import android.util.Base64;
import android.support.v7.widget.SearchView;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


interface MainDataReceived {
    void mainDataReceived(boolean success);
}

class MainDataFetch {
    private final MainActivity mainActivity;
    private final JSONObject completeData;
    private final String credentials;
    private final String serverString;
    private MainDataReceived myMainDataReceived;


    MainDataFetch(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.completeData = new JSONObject();

        final String[] prefs = Tools.getLogin(mainActivity);

        serverString = prefs[0];
        String username = prefs[1];
        String password = prefs[2];

        // Create credentials Header
        credentials = String.format("Basic %s", Base64.encodeToString(String.format("%s:%s", username, password).getBytes(), Base64.DEFAULT));
    }

    void refresh(MainDataReceived refreshCallback) {
        this.myMainDataReceived = refreshCallback;
        // Reset the search bar
        SearchView searchView = mainActivity.getSearchView();
        if (searchView != null) {
            searchView.onActionViewCollapsed();
            searchView.setQuery("", false);
            searchView.clearFocus();
        }


        if (Tools.isConnected(mainActivity)){
            VolleySingleton.getInstance(mainActivity).getRequestQueue();

            // Create the request URLs
            String statusURL = serverString + "/v1/status/CIB";
            String hostURL = serverString + "/v1/objects/hosts?attrs=last_check_result&attrs=state&attrs=name&attrs=acknowledgement&attrs=enable_notifications";
            String serviceURL = serverString + "/v1/objects/services?attrs=last_check_result&attrs=state&attrs=name&attrs=host_name&attrs=last_state&attrs=last_state_change&attrs=enable_notifications&attrs=acknowledgement";
            String commentsURL = serverString + "/v1/objects/comments?attrs=author&attrs=host_name&attrs=service_name&attrs=text";

            // Fire off the requests
            sendRequest(statusURL, "status");
            sendRequest(hostURL, "hosts");
            sendRequest(serviceURL, "services");
            sendRequest(commentsURL, "comments");

        } else {

            Toast.makeText(mainActivity, R.string.no_connectivity, Toast.LENGTH_LONG).show();
            refreshCallback.mainDataReceived(false);
        }
    }

    private void sendRequest(final String url, final String jsonPart) {

        JsonObjectRequest mainDataRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {

                    // Add each response to the right place in completeData
                    if (jsonPart.equals("status")) {
                        completeData.put(jsonPart, response.getJSONArray("results").getJSONObject(0).getJSONObject("status"));
                    } else {
                        completeData.put(jsonPart, response.getJSONArray("results"));
                    }

                    // When all answers are combined, parse the data and update the list fragments
                    if (completeData.has("status")
                            && completeData.has("hosts")
                            && completeData.has("services")
                            && completeData.has("comments")) {
                        try {
                            Tools.parseData(completeData);
                            myMainDataReceived.mainDataReceived(true);
                        } catch (JSONException e) {
                            Toast.makeText(mainActivity, "Unable to parse response", Toast.LENGTH_LONG).show();
                            myMainDataReceived.mainDataReceived(false);
                        }
                    }
                } catch (JSONException e) {
                    Toast.makeText(mainActivity, "Failed to parse JSON", Toast.LENGTH_LONG).show();
                    myMainDataReceived.mainDataReceived(false);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                myMainDataReceived.mainDataReceived(false);

                // Handle various kinds of Network errors
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Toast.makeText(mainActivity,"Connection Timeout", Toast.LENGTH_LONG).show();
                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(mainActivity,"Auth Error", Toast.LENGTH_LONG).show();
                } else if (error instanceof ServerError) {
                    Toast.makeText(mainActivity,"Server Error", Toast.LENGTH_LONG).show();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(mainActivity,"Network Error", Toast.LENGTH_LONG).show();
                } else if (error instanceof ParseError) {
                    Toast.makeText(mainActivity,"Could not parse response", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mainActivity,"Could not get data", Toast.LENGTH_LONG).show();
                }
            }
        }) {

            // Set HTTP headers
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", credentials);
                params.put("Accept", "application/json");
                return params;
            }
        };

        // Add the constructed request to the queue
        VolleySingleton.getInstance(mainActivity).addToRequestQueue(mainDataRequest);
    }

}
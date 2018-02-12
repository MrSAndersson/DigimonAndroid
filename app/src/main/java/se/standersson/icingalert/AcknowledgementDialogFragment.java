package se.standersson.icingalert;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds the Acknowledgement Dialog
 */

public class AcknowledgementDialogFragment extends DialogFragment implements MainDataReceived{
    private int groupPosition;
    private int childPosition;
    private List <HostAbstract> hosts;
    private MainPagerAdapter mainPagerAdapter;

    static AcknowledgementDialogFragment newInstance(List<HostAbstract> hosts, int groupPosition, int childPosition) {
        AcknowledgementDialogFragment fragment = new AcknowledgementDialogFragment();
        Bundle args = new Bundle();
        args.putInt("groupPosition", groupPosition);
        args.putInt("childPosition", childPosition);
        args.putBoolean("isService", true);
        args.putSerializable("hosts", (Serializable) hosts);
        fragment.setArguments(args);
        return fragment;
    }

    static AcknowledgementDialogFragment newInstance(List<HostAbstract> hosts, int groupPosition) {
        AcknowledgementDialogFragment fragment = new AcknowledgementDialogFragment();
        Bundle args = new Bundle();
        args.putInt("groupPosition", groupPosition);

        args.putSerializable("hosts", (Serializable) hosts);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        final boolean isService = getArguments().getBoolean("isService", false);

        //Set mainPagerAdapter (Needed to update fragments)
        mainPagerAdapter = ((MainActivity) getActivity()).getMainPagerAdapter();

        groupPosition = getArguments().getInt("groupPosition");

        // If Acknowledging a service, set a childPosition
        if (isService) {
            childPosition = getArguments().getInt("childPosition");
        } else {
            childPosition = 0;
        }
        // noinspection unchecked
        hosts = (List<HostAbstract>) getArguments().getSerializable("hosts");

        // Build a dialog using the AlertDialog.Builder class
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.acknowledgement_dialog_title);

        // Configure the view
        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams") final View view = inflater.inflate(R.layout.acknowledgement_layout, null);
        assert hosts != null;
        if (isService) {
            String serviceString = hosts.get(groupPosition).getServiceName(childPosition);
            ((TextView) view.findViewById(R.id.acknowledgement_servicename)).setText(serviceString);
        }
        String hostString = hosts.get(groupPosition).getHostName();
        ((TextView)view.findViewById(R.id.acknowledgement_hostname)).setText(hostString);

        builder.setView(view)
         // Add action buttons
           .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int id) {

                   // Assemble all needed information in order to send it to the server
                   EditText commentView = view.findViewById(R.id.acknowledgement_comment);
                   String comment = commentView.getText().toString();
                   boolean notify = ((CheckBox) view.findViewById(R.id.acknowledgement_notify)).isChecked();

                   SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                   String author = prefs.getString("acknowledgement_author", "");

                   // Send the acknowledgement
                   sendAcknowledgement(author, comment, isService, notify);
               }
           })
           .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                   AcknowledgementDialogFragment.this.getDialog().cancel();
               }
           });
    return builder.create();
    }

    private void sendAcknowledgement(final String author, final String comment, final boolean isService, boolean notify) {
        final MainActivity activity = (MainActivity) getActivity();
        final AcknowledgementDialogFragment thisClass = this;

        if (Tools.isConnected(getActivity())) {
            String hostName =  hosts.get(groupPosition).getHostName();
            String serviceIdentifier = hostName + "!" + hosts.get(groupPosition).getServiceName(childPosition);

            VolleySingleton.getInstance(getActivity()).getRequestQueue();
            final String[] prefsString = Tools.getLogin(getActivity());
            final String requestString;

            if (isService) {
                requestString = prefsString[0] + "/v1/actions/acknowledge-problem?type=" + "Service&service=" + serviceIdentifier;
            } else {
                requestString = prefsString[0] + "/v1/actions/acknowledge-problem?type=" + "Host&host=" + hostName;
            }

            JSONObject actionJSON = new JSONObject();
            try {
                actionJSON.put("author", author);
                actionJSON.put("comment", comment);
                actionJSON.put("notify", notify);
                actionJSON.put("sticky", true);
            } catch (JSONException e) {
                Log.d("MainList: ", "JSONException");
            }

            JsonObjectRequest notificationChangeRequest = new JsonObjectRequest(Request.Method.POST, requestString, actionJSON, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {

                    try {
                        String status = response.getJSONArray("results").getJSONObject(0).getString("status");
                        if (status.contains("is UP")) {
                            Toast.makeText(activity, "The host is already up", Toast.LENGTH_LONG).show();
                        } else if (status.contains("is OK")) {
                            Toast.makeText(activity, "The Service has recovered", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(activity, "Acknowledgement set", Toast.LENGTH_LONG).show();
                        }

                        // Show the update spinners on the main ExpandableListViews
                        mainPagerAdapter.getFragment(1).setRefreshSpinner(true);
                        mainPagerAdapter.getFragment(0).setRefreshSpinner(true);
                        mainPagerAdapter.getFragment2(1).setRefreshSpinner(true);
                        mainPagerAdapter.getFragment2(0).setRefreshSpinner(true);

                        // Update Data form the server
                        new MainDataFetch(activity).refresh(thisClass);

                    } catch (JSONException e) {
                        Log.d("MainList: ", "JSONException");
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getActivity(), "Could not update server: " + error.getMessage(), Toast.LENGTH_LONG).show();
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
            VolleySingleton.getInstance(getActivity()).addToRequestQueue(notificationChangeRequest);
        } else {
            Toast.makeText(getActivity(), R.string.no_connectivity, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void mainDataReceived(boolean success) {

        // Remove the update spinners from the main ExpandableListViews
        mainPagerAdapter.getFragment(0).setRefreshSpinner(false);
        mainPagerAdapter.getFragment(1).setRefreshSpinner(false);
        mainPagerAdapter.getFragment2(0).setRefreshSpinner(false);
        mainPagerAdapter.getFragment2(1).setRefreshSpinner(false);

        if (success) {
            mainPagerAdapter.getFragment(0).update(Tools.filterProblems(HostSingleton.getInstance().getHosts()));
            mainPagerAdapter.getFragment(1).update(Tools.fullHostList(HostSingleton.getInstance().getHosts()));
            mainPagerAdapter.getFragment2(0).update(Tools.filterProblems(HostSingleton.getInstance().getHosts()));
            mainPagerAdapter.getFragment2(1).update(Tools.fullHostList(HostSingleton.getInstance().getHosts()));
        }
    }
}

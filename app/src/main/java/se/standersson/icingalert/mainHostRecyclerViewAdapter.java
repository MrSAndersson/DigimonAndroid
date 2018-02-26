package se.standersson.icingalert;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class mainHostRecyclerViewAdapter extends RecyclerView.Adapter<mainHostRecyclerViewAdapter.ViewHolder> {

    private final Context context;
    private List<HostAbstract> hosts;
    private RecyclerView parentRecyclerView;
    private final RecyclerView.RecycledViewPool recycledViewPool;
    private android.support.transition.Transition transition;


    mainHostRecyclerViewAdapter(Context context, List<HostAbstract> hosts) {
        this.hosts = hosts;
        this.recycledViewPool = new RecyclerView.RecycledViewPool();
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_hostlist_cardview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.host = hosts.get(position);
        holder.hostName.setText(holder.host.getHostName());

        if (holder.host.isExpanded()) {
            holder.serviceList.setVisibility(View.VISIBLE);
        } else {
            holder.serviceList.setVisibility(View.GONE);
        }

        // Set hostName background
        if (holder.host.isDown()) {
            if (holder.host.isAcknowledged()) {
                holder.hostName.setBackground(context.getDrawable(R.drawable.host_down_ack_box));
            } else {
                holder.hostName.setBackground(context.getDrawable(R.drawable.host_down_box));
            }
        } else {
            holder.hostName.setBackgroundColor(0x00000000);
        }

        // Set hostComment
        if (holder.host.getComment().equals("")) {
            holder.hostComment.setVisibility(View.GONE);
        } else {
            holder.hostComment.setVisibility(View.VISIBLE);
            String comment = "Comment:\n" + holder.host.getComment() + "\n/" + holder.host.getCommentAuthor();
            holder.hostComment.setText(comment);
        }

        // Set and show the number of failing services
        setHostStatusCounters(holder);

        // Initiate Service List
        holder.serviceList.setAdapter(new mainServiceRecyclerViewAdapter(context, holder.host));

        holder.moreButton.setOnClickListener(new hostMoreMenu(holder));

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.host.isExpanded()) {
                    holder.serviceList.setVisibility(View.GONE);
                    holder.host.setExpanded(false);
                } else {
                    holder.serviceList.setVisibility(View.VISIBLE);
                    holder.host.setExpanded(true);
                }
                // Stop all currently running transitions and start a new one
                android.support.transition.TransitionManager.endTransitions(parentRecyclerView);
                android.support.transition.TransitionManager.beginDelayedTransition(parentRecyclerView, transition);
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.parentRecyclerView = recyclerView;
        transition = android.support.transition.TransitionInflater.from(context).inflateTransition(R.transition.main_list_transition);
    }

    @Override
    public int getItemCount() {
        return hosts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View view;
        final TextView hostName;
        final TextView hostComment;
        final TextView criticalCount;
        final TextView criticalAckCount;
        final TextView warningCount;
        final TextView warningAckCount;
        final TextView unknownCount;
        final TextView unknownAckCount;
        final ImageButton moreButton;
        final RecyclerView serviceList;
        HostAbstract host;

        ViewHolder(View view) {
            super(view);
            this.view = view;
            hostName = view.findViewById(R.id.main_list_hostname);
            hostComment = view.findViewById(R.id.main_list_host_comment);
            criticalCount = view.findViewById(R.id.main_list_critical_count);
            criticalAckCount = view.findViewById(R.id.main_list_critical_ack_count);
            warningCount = view.findViewById(R.id.main_list_warning_count);
            warningAckCount = view.findViewById(R.id.main_list_warning_ack_count);
            unknownCount = view.findViewById(R.id.main_list_unknown_count);
            unknownAckCount = view.findViewById(R.id.main_list_unknown_ack_count);
            moreButton = view.findViewById(R.id.main_list_more_button);
            serviceList = view.findViewById(R.id.service_list);
            serviceList.setRecycledViewPool(recycledViewPool);
        }
    }

    void updateHostList(List<HostAbstract> hosts){
        this.hosts = hosts;
    }

    private void setHostStatusCounters(ViewHolder holder) {

        // Set and show the number of failing services

        Integer stateCount = holder.host.getStateCount(1);
        Integer stateAckCount = holder.host.getStateAckCount(1);
        String stateCountString;


        if (stateCount == 0 && stateAckCount == 0) {
            holder.warningCount.setVisibility(View.GONE);
            holder.warningAckCount.setVisibility(View.GONE);
        } else {
            if (stateCount == 0) {
                stateCountString = "";
            } else {
                stateCountString = stateCount.toString();
            }
            holder.warningCount.setText(stateCountString);

            if (stateAckCount == 0) {
                stateCountString = "";
            } else {
                stateCountString = stateAckCount.toString();
            }
            holder.warningAckCount.setText(stateCountString);
            holder.warningCount.setVisibility(View.VISIBLE);
            holder.warningAckCount.setVisibility(View.VISIBLE);
        }

        stateCount = holder.host.getStateCount(2);
        stateAckCount = holder.host.getStateAckCount(2);

        if (stateCount == 0 && stateAckCount == 0) {
            holder.criticalCount.setVisibility(View.GONE);
            holder.criticalAckCount.setVisibility(View.GONE);
        } else {
            if (stateCount == 0) {
                stateCountString = "";
            } else {
                stateCountString = stateCount.toString();
            }
            holder.criticalCount.setText(stateCountString);

            if (stateAckCount == 0) {
                stateCountString = "";
            } else {
                stateCountString = stateAckCount.toString();
            }
            holder.criticalAckCount.setText(stateCountString);
            holder.criticalCount.setVisibility(View.VISIBLE);
            holder.criticalAckCount.setVisibility(View.VISIBLE);
        }

        stateCount = holder.host.getStateCount(3);
        stateAckCount = holder.host.getStateAckCount(3);

        if (stateCount == 0 && stateAckCount == 0) {
            holder.unknownCount.setVisibility(View.GONE);
            holder.unknownAckCount.setVisibility(View.GONE);
        } else {
            if (stateCount == 0) {
                stateCountString = "";
            } else {
                stateCountString = stateCount.toString();
            }
            holder.unknownCount.setText(stateCountString);

            if (stateAckCount == 0) {
                stateCountString = "";
            } else {
                stateCountString = stateAckCount.toString();
            }
            holder.unknownAckCount.setText(stateCountString);
            holder.unknownCount.setVisibility(View.VISIBLE);
            holder.unknownAckCount.setVisibility(View.VISIBLE);
        }
    }

    class hostMoreMenu implements View.OnClickListener{
        final ViewHolder holder;

        hostMoreMenu(ViewHolder holder) {
            this.holder = holder;
        }
        @Override
        public void onClick(View v) {
            PopupMenu popup = new PopupMenu(context, v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.main_list_host_more_menu, popup.getMenu());

            popup.getMenu().findItem(R.id.host_notifying).setChecked(holder.host.isNotifying());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {

                        case R.id.host_acknowledge:
                            // Check that user has set Acknowledgement Author in the app settings
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                            if (prefs.getString("acknowledgement_author", "").equals("")) {
                                Toast.makeText(context, "No Author set. Set Acknowledgement Author in the Settings", Toast.LENGTH_SHORT).show();
                            } else if (!holder.host.isDown()) {
                                // If host is already up, don't show dialogue
                                Toast.makeText(context, "Host is up, no need to Acknowledge", Toast.LENGTH_SHORT).show();
                            } else {

                                AcknowledgementDialogFragment dialog = AcknowledgementDialogFragment.newInstance(holder.host);

                                try {
                                    final AppCompatActivity activity = (AppCompatActivity) context;
                                    dialog.show(activity.getFragmentManager(), "acknowledgementDialog");
                                } catch (ClassCastException e) {
                                    Log.d("Error", "Can't get the fragment manager with this");
                                }
                            }
                            break;

                        case R.id.host_notifying:
                            // Invert checked state since it was clicked
                            item.setChecked(!item.isChecked());
                            updateNotifying(item);
                            break;

                        default:
                            break;
                    }
                    return true;
                }
            });
            popup.show();
        }

        private void updateNotifying(final MenuItem item) {

            final boolean isChecked = item.isChecked();

            if (Tools.isConnected(context)) {

                VolleySingleton.getInstance(context).getRequestQueue();
                final String[] prefsString = Tools.getLogin(context);

                final String requestString = prefsString[0] + "/v1/objects/hosts?host=" + holder.host.getHostName();

                JSONObject actionJSON = new JSONObject();
                try {
                    actionJSON.put("attrs", new JSONObject());
                    actionJSON.getJSONObject("attrs").put("enable_notifications", isChecked);
                } catch (JSONException e) {
                    Log.d("MainList: ", "JSONException");
                }

                JsonObjectRequest notificationChangeRequest = new JsonObjectRequest(Request.Method.POST, requestString, actionJSON, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        // Display the result
                        if (isChecked) {
                            Toast.makeText(context, "Notifications Enabled!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Notifications Disabled!", Toast.LENGTH_SHORT).show();
                        }
                        holder.host.setNotifying(isChecked);
                    }
                }, new Response.ErrorListener() {

                    // If the server replies with an error
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Could not update server: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {

                    // Override the request headers to add credentials
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        String credentials = String.format("Basic %s", Base64.encodeToString(String.format("%s:%s", prefsString[1], prefsString[2]).getBytes(), Base64.DEFAULT));
                        params.put("Authorization", credentials);
                        params.put("Accept", "application/json");
                        return params;
                    }
                };


                VolleySingleton.getInstance(context).addToRequestQueue(notificationChangeRequest);
            } else {
                Toast.makeText(context, R.string.no_connectivity, Toast.LENGTH_SHORT).show();
            }


        }
    }


}
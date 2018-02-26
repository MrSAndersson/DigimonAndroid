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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class mainServiceRecyclerViewAdapter extends RecyclerView.Adapter<mainServiceRecyclerViewAdapter.ViewHolder> {
    private final HostAbstract host;
    private final Context context;
    private RecyclerView parentRecyclerView;
    private android.support.transition.Transition transition;

    mainServiceRecyclerViewAdapter(Context context, HostAbstract host) {
        this.context = context;
        this.host = host;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_servicelist_cardview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final mainServiceRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.service = host.getService(position);
        holder.serviceName.setText(holder.service.getServiceName());

        // Set Last State Change
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm  dd/MM/yy", Locale.getDefault());
        String timeString = dateFormat.format(new Date(holder.service.getLastStateChange() * 1000));
        holder.lastStateChange.setText(timeString);

        // Set Service Details
        holder.serviceDetails.setText(holder.service.getDetails());

        // Configure Service Comment
        if (!holder.service.getComment().equals("") && !holder.service.getCommentAuthor().equals(""))
        {
            String comment = "Comment:\n" + holder.service.getComment() + "\n/" + holder.service.getCommentAuthor();
            holder.serviceComment.setText(comment);
        } else {
            holder.serviceComment.setText("");
        }

        if (holder.serviceComment.getText() == "") {
            holder.serviceComment.setVisibility(View.GONE);
        } else {
            holder.serviceComment.setVisibility(View.VISIBLE);
        }

        if (holder.service.isExpanded()) {
            holder.view.findViewById(R.id.main_list_service_exp).setVisibility(View.VISIBLE);
            holder.lastStateChange.setVisibility(View.VISIBLE);
        } else {
            holder.view.findViewById(R.id.main_list_service_exp).setVisibility(View.GONE);
            holder.lastStateChange.setVisibility(View.GONE);
        }

        // Configure Status Indicator

        final int OK = 0;
        final int WARNING = 1;
        final int CRITICAL = 2;
        final int UNKNOWN = 3;

        switch (holder.service.getState()) {
            case OK:
                holder.statusIndicator.setVisibility(View.INVISIBLE);
                break;
            case WARNING:
                if (holder.service.isAcknowledged()) {
                    holder.statusIndicator.setImageDrawable(context.getDrawable(R.drawable.service_warning_ack_indicator));
                } else {
                    holder.statusIndicator.setImageDrawable(context.getDrawable(R.drawable.service_warning_indicator));
                }
                holder.statusIndicator.setVisibility(View.VISIBLE);
                break;
            case CRITICAL:
                if (holder.service.isAcknowledged()) {
                    holder.statusIndicator.setImageDrawable(context.getDrawable(R.drawable.service_critical_ack_indicator));
                } else {
                    holder.statusIndicator.setImageDrawable(context.getDrawable(R.drawable.service_critical_indicator));
                }
                holder.statusIndicator.setVisibility(View.VISIBLE);
                break;
            case UNKNOWN:
                if (holder.service.isAcknowledged()) {
                    holder.statusIndicator.setImageDrawable(context.getDrawable(R.drawable.service_unknown_ack_indicator));
                } else {
                    holder.statusIndicator.setImageDrawable(context.getDrawable(R.drawable.service_unknown_indicator));
                }
                holder.statusIndicator.setVisibility(View.VISIBLE);
                break;
            default:
                holder.statusIndicator.setVisibility(View.INVISIBLE);
                break;
        }



        holder.moreMenu.setOnClickListener(new serviceMoreMenu(holder));

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.service.isExpanded()) {
                    holder.view.findViewById(R.id.main_list_service_exp).setVisibility(View.GONE);
                    holder.lastStateChange.setVisibility(View.GONE);
                    holder.service.setIsExpanded(false);
                } else {
                    holder.view.findViewById(R.id.main_list_service_exp).setVisibility(View.VISIBLE);
                    holder.lastStateChange.setVisibility(View.VISIBLE);
                    holder.service.setIsExpanded(true);
                }
                // Stop all currently running transitions and start a new one
                android.support.transition.TransitionManager.endTransitions(parentRecyclerView);
                android.support.transition.TransitionManager.beginDelayedTransition(parentRecyclerView, transition);
            }
        });

    }

    @Override
    public int getItemCount() {
        return host.getServiceCount();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View view;
        final TextView serviceName;
        final TextView lastStateChange;
        final ImageButton moreMenu;
        final TextView serviceDetails;
        final TextView serviceComment;
        final ImageView statusIndicator;

        Service service;

        ViewHolder(View view) {
            super(view);
            this.view = view;
            serviceName = view.findViewById(R.id.main_list_service_name);
            lastStateChange = view.findViewById(R.id.main_list_service_last_state_change);
            moreMenu = view.findViewById(R.id.main_list_service_more_button);
            serviceDetails = view.findViewById(R.id.main_list_service_details);
            serviceComment = view.findViewById(R.id.main_list_service_comment);
            statusIndicator = view.findViewById(R.id.main_list_service_indicator);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.parentRecyclerView = recyclerView;
        transition = android.support.transition.TransitionInflater.from(context).inflateTransition(R.transition.main_list_transition);
    }

    class serviceMoreMenu implements View.OnClickListener{
        final mainServiceRecyclerViewAdapter.ViewHolder holder;

        serviceMoreMenu(mainServiceRecyclerViewAdapter.ViewHolder holder) {
            this.holder = holder;
        }
        @Override
        public void onClick(View v) {
            PopupMenu popup = new PopupMenu(context, v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.main_list_service_more_menu, popup.getMenu());

            popup.getMenu().findItem(R.id.service_notifying).setChecked(holder.service.isNotifying());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {

                        case R.id.service_acknowledge:
                            // Check that user has set Acknowledgement Author in the app settings
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                            if (prefs.getString("acknowledgement_author", "").equals("")) {
                                Toast.makeText(context, "No Author set. Set Acknowledgement Author in the Settings", Toast.LENGTH_SHORT).show();
                            } else if (holder.service.getState() != 1) {
                                // If service is OK, don't show dialogue
                                Toast.makeText(context, "Service is OK, no need to Acknowledge", Toast.LENGTH_SHORT).show();
                            } else {

                                AcknowledgementDialogFragment dialog = AcknowledgementDialogFragment.newInstance(host, holder.service);

                                try {
                                    final AppCompatActivity activity = (AppCompatActivity) context;
                                    dialog.show(activity.getFragmentManager(), "acknowledgementDialog");
                                } catch (ClassCastException e) {
                                    Log.d("Error", "Can't get the fragment manager with this");
                                }
                            }
                            break;

                        case R.id.service_notifying:
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

                final String requestString = prefsString[0] + "/v1/objects/services?service=" + host.getHostName() + "!" + holder.service.getServiceName();

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
                        holder.service.setIsNotifying(isChecked);
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

package se.standersson.icingalert;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;


class mainExpandableListAdapter extends BaseExpandableListAdapter {
    private final Context context;
    private final List<Host> hosts;


    mainExpandableListAdapter(Context context, List<Host> hosts) {
        this.context = context;
        this.hosts = hosts;
    }

    @Override
    public int getGroupCount() {
        return hosts.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return hosts.get(groupPosition).getServiceCount();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return hosts.get(groupPosition).getHostName();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return hosts.get(groupPosition).getServiceName(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        GroupViewHolder groupViewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.main_expanded_list_group, parent, false);
            groupViewHolder = new GroupViewHolder(convertView);
            convertView.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupViewHolder) convertView.getTag();
        }


            /*
            * Show what hosts are down and how many services are down
             */

        if (hosts.get(groupPosition).isDown()){
            groupViewHolder.downHostName.setText(hosts.get(groupPosition).getHostName());
            groupViewHolder.downHostName.setVisibility(View.VISIBLE);
            groupViewHolder.hostName.setVisibility(View.GONE);

            if (hosts.get(groupPosition).isAcknowledged()) {
                groupViewHolder.downHostName.setBackground(context.getDrawable(R.drawable.host_down_ack_box));
            } else {
                groupViewHolder.downHostName.setBackground(context.getDrawable(R.drawable.host_down_box));
            }

        } else {
            groupViewHolder.hostName.setText(hosts.get(groupPosition).getHostName());
            groupViewHolder.hostName.setVisibility(View.VISIBLE);
            groupViewHolder.downHostName.setVisibility(View.GONE);
        }

        // Set and show the number of failing services

        Integer stateCount = hosts.get(groupPosition).getStateCount(1);
        Integer stateAckCount = hosts.get(groupPosition).getStateAckCount(1);
        String stateCountString;


        if (stateCount == 0 && stateAckCount == 0) {
            groupViewHolder.warningCount.setVisibility(View.GONE);
            groupViewHolder.warningAckCount.setVisibility(View.GONE);
        } else {
            if (stateCount == 0) {
                stateCountString = "";
            } else {
                stateCountString = stateCount.toString();
            }
            groupViewHolder.warningCount.setText(stateCountString);

            if (stateAckCount == 0) {
                stateCountString = "";
            } else {
                stateCountString = stateAckCount.toString();
            }
            groupViewHolder.warningAckCount.setText(stateCountString);
            groupViewHolder.warningCount.setVisibility(View.VISIBLE);
            groupViewHolder.warningAckCount.setVisibility(View.VISIBLE);
        }

        stateCount = hosts.get(groupPosition).getStateCount(2);
        stateAckCount = hosts.get(groupPosition).getStateAckCount(2);

        if (stateCount == 0 && stateAckCount == 0) {
            groupViewHolder.criticalCount.setVisibility(View.GONE);
            groupViewHolder.criticalAckCount.setVisibility(View.GONE);
        } else {
            if (stateCount == 0) {
                stateCountString = "";
            } else {
                stateCountString = stateCount.toString();
            }
            groupViewHolder.criticalCount.setText(stateCountString);

            if (stateAckCount == 0) {
                stateCountString = "";
            } else {
                stateCountString = stateAckCount.toString();
            }
            groupViewHolder.criticalAckCount.setText(stateCountString);
            groupViewHolder.criticalCount.setVisibility(View.VISIBLE);
            groupViewHolder.criticalAckCount.setVisibility(View.VISIBLE);
        }

        stateCount = hosts.get(groupPosition).getStateCount(3);
        stateAckCount = hosts.get(groupPosition).getStateAckCount(3);

        if (stateCount == 0 && stateAckCount == 0) {
            groupViewHolder.unknownCount.setVisibility(View.GONE);
            groupViewHolder.unknownAckCount.setVisibility(View.GONE);
        } else {
            if (stateCount == 0) {
                stateCountString = "";
            } else {
                stateCountString = stateCount.toString();
            }
            groupViewHolder.unknownCount.setText(stateCountString);

            if (stateAckCount == 0) {
                stateCountString = "";
            } else {
                stateCountString = stateAckCount.toString();
            }
            groupViewHolder.unknownAckCount.setText(stateCountString);
            groupViewHolder.unknownCount.setVisibility(View.VISIBLE);
            groupViewHolder.unknownAckCount.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder viewHolder;

        /*
        * If it's the first time the child is created, create the view and store the view
        * so that we don't need to find it every time we re-create it.
        */
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.main_expanded_list_item, parent, false);
            viewHolder = new ChildViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ChildViewHolder) convertView.getTag();
        }
        viewHolder.serviceName.setText(hosts.get(groupPosition).getServiceName(childPosition));
        viewHolder.serviceDetails.setText(hosts.get(groupPosition).getServiceDetails(childPosition));
        viewHolder.serviceNotifications.setChecked(hosts.get(groupPosition).isServiceNotifying(childPosition));
        viewHolder.serviceComment.setText(hosts.get(groupPosition).getServiceComment(childPosition));
        if (viewHolder.serviceComment.getText() == "") {
            viewHolder.serviceComment.setVisibility(View.GONE);
        } else {
            viewHolder.serviceComment.setVisibility(View.VISIBLE);
        }

        viewHolder.serviceNotifications.setOnClickListener(new onNotificationUpdate(viewHolder.serviceNotifications, groupPosition, childPosition, context));
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm  dd/MM/yy", Locale.getDefault());
        String timeString = dateFormat.format(new Date(hosts.get(groupPosition).getServiceLastStateChange(childPosition) * 1000));
        viewHolder.lastStateChange.setText(timeString);

        // Show the right color of bar to the left of the service name
        switch (hosts.get(groupPosition).getServiceState(childPosition)){
            case 1:
                if (hosts.get(groupPosition).isServiceAcknowledged(childPosition)) {
                    viewHolder.stateBar.setBackground(context.getDrawable(R.drawable.warning_bar_ack));
                } else {
                    viewHolder.stateBar.setBackground(context.getDrawable(R.drawable.warning_bar));
                }
                viewHolder.stateBar.setVisibility(View.VISIBLE);

                break;
            case 2:
                if (hosts.get(groupPosition).isServiceAcknowledged(childPosition)) {
                    viewHolder.stateBar.setBackground(context.getDrawable(R.drawable.critical_bar_ack));
                } else {
                    viewHolder.stateBar.setBackground(context.getDrawable(R.drawable.critical_bar));
                }
                viewHolder.stateBar.setVisibility(View.VISIBLE);
                break;
            case 3:
                if (hosts.get(groupPosition).isServiceAcknowledged(childPosition))
                {
                    viewHolder.stateBar.setBackground(context.getDrawable(R.drawable.unknown_bar_ack));
                } else {
                    viewHolder.stateBar.setBackground(context.getDrawable(R.drawable.unknown_bar));
                }
                viewHolder.stateBar.setVisibility(View.VISIBLE);
                break;
            default:
                viewHolder.stateBar.setVisibility(View.INVISIBLE);
                break;
        }

        if (hosts.get(groupPosition).isServiceExpanded(childPosition)) {
            viewHolder.childExpand.setVisibility(View.VISIBLE);
            viewHolder.expandArrow.setRotation(180);
        } else {
            viewHolder.childExpand.setVisibility(View.GONE);
            viewHolder.expandArrow.setRotation(0);
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    void childClick(int groupPosition, int childPosition, View view) {
        ChildViewHolder viewHolder = (ChildViewHolder) view.getTag();
        if (hosts.get(groupPosition).isServiceExpanded(childPosition)) {
            viewHolder.childExpand.setVisibility(View.GONE);
            hosts.get(groupPosition).setServiceExpanded(childPosition, false);
            viewHolder.expandArrow.animate().rotation(0);
        } else {
            viewHolder.childExpand.setVisibility(View.VISIBLE);
            hosts.get(groupPosition).setServiceExpanded(childPosition, true);
            viewHolder.expandArrow.animate().rotation(180);
        }
    }


    private class GroupViewHolder{
        final TextView hostName;
        final TextView downHostName;
        final TextView criticalCount;
        final TextView criticalAckCount;
        final TextView warningCount;
        final TextView warningAckCount;
        final TextView unknownCount;
        final TextView unknownAckCount;


        GroupViewHolder(View view){
            hostName = view.findViewById(R.id.main_exp_list_hostname);
            downHostName = view.findViewById(R.id.main_exp_list_hostname_down);
            criticalCount = view.findViewById(R.id.main_exp_list_critical_count);
            criticalAckCount = view.findViewById(R.id.main_exp_list_critical_ack_count);
            warningCount = view.findViewById(R.id.main_exp_list_warning_count);
            warningAckCount = view.findViewById(R.id.main_exp_list_warning_ack_count);
            unknownCount = view.findViewById(R.id.main_exp_list_unknown_count);
            unknownAckCount = view.findViewById(R.id.main_exp_list_unknown_ack_count);
        }
    }

    private class ChildViewHolder{
        final TextView serviceName;
        final TextView serviceDetails;
        final TextView stateBar;
        final LinearLayout childExpand;
        final CheckBox serviceNotifications;
        final TextView lastStateChange;
        final TextView serviceComment;
        final ImageView expandArrow;


        ChildViewHolder(View view){
            serviceName = view.findViewById(R.id.expanded_list_item);
            serviceDetails = view.findViewById(R.id.exp_service_details);
            stateBar = view.findViewById(R.id.state_bar);
            childExpand = view.findViewById(R.id.child_expand);
            serviceNotifications = view.findViewById(R.id.service_notifications);
            lastStateChange = view.findViewById(R.id.expanded_list_item_last_state_changed);
            serviceComment = view.findViewById(R.id.service_comment);
            expandArrow = view.findViewById(R.id.child_expand_icon);
        }
    }

    private class onNotificationUpdate implements  View.OnClickListener {
        final int groupPosition;
        final int childPosition;
        final Context context;
        final CompoundButton button;

        onNotificationUpdate(CompoundButton button, int groupPosition, int childPosition, Context context) {
            this.button = button;
            this.groupPosition = groupPosition;
            this.childPosition = childPosition;
            this.context = context;
        }


        @Override
        public void onClick(View view) {
            final boolean isChecked = button.isChecked();
            if (Tools.isConnected(context)) {
                String serviceIdentifier = hosts.get(groupPosition).getHostName() + "!" + hosts.get(groupPosition).getServiceName(childPosition);

                VolleySingleton.getInstance(context).getRequestQueue();
                final String[] prefsString = Tools.getLogin(context);

                final String requestString = prefsString[0] + "/v1/objects/services?service=" + serviceIdentifier;

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
                        if (isChecked) {
                            Toast.makeText(context, "Notifications Enabled!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Notifications Disabled!", Toast.LENGTH_SHORT).show();
                        }
                        hosts.get(groupPosition).setServiceNotifying(childPosition, isChecked);
                        int singletonHostPos = HostSingleton.getInstance().findHostName(hosts.get(groupPosition).getHostName());
                        int singletonServicePos = HostSingleton.getInstance().findServiceName(singletonHostPos, hosts.get(groupPosition).getServiceName(childPosition));
                        HostSingleton.getInstance().setServiceNotifying(singletonHostPos, singletonServicePos, isChecked);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Could not update server: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        button.setChecked(!isChecked);
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


                VolleySingleton.getInstance(context).addToRequestQueue(notificationChangeRequest);
            } else {
                Toast.makeText(context, R.string.no_connectivity, Toast.LENGTH_SHORT).show();
                button.setChecked(!isChecked);
            }
        }
    }

}






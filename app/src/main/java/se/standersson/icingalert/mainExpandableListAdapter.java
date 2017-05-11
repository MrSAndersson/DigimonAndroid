package se.standersson.icingalert;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;


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
        } else {
            groupViewHolder.hostName.setText(hosts.get(groupPosition).getHostName());
            groupViewHolder.hostName.setVisibility(View.VISIBLE);
            groupViewHolder.downHostName.setVisibility(View.GONE);
        }

        Integer stateCount = hosts.get(groupPosition).getStateCount(1);
        String stateCountString;

        if (stateCount == 0) {
            groupViewHolder.warningCount.setVisibility(View.GONE);
        } else {
            stateCountString = stateCount.toString();
            groupViewHolder.warningCount.setText(stateCountString);
            groupViewHolder.warningCount.setVisibility(View.VISIBLE);
        }
        stateCount = hosts.get(groupPosition).getStateCount(2);
        if (stateCount == 0) {
            groupViewHolder.criticalCount.setVisibility(View.GONE);
        } else {
            stateCountString = stateCount.toString();
            groupViewHolder.criticalCount.setText(stateCountString);
            groupViewHolder.criticalCount.setVisibility(View.VISIBLE);
        }
        stateCount = hosts.get(groupPosition).getStateCount(3);
        if (stateCount == 0) {
            groupViewHolder.unknownCount.setVisibility(View.GONE);
        } else {
            stateCountString = stateCount.toString();
            groupViewHolder.unknownCount.setText(stateCountString);
            groupViewHolder.unknownCount.setVisibility(View.VISIBLE);
        }


        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
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
        /*SimpleDateFormat sdf = new SimpleDateFormat("hh:mm dd/MMM");
        viewHolder.lastStateChange.setText(sdf.format(hosts.get(groupPosition).getServiceLastStateChange(childPosition)));
        switch (hosts.get(groupPosition).getServiceLastState(childPosition)) {
            case 0:
                viewHolder.lastState.setText("OK");
                break;
            case 1:
                viewHolder.lastState.setText("Warning");
                break;
            case 2:
                viewHolder.lastState.setText("Critical");
                break;
            case 3:
                viewHolder.lastState.setText("Unknown");
                break;
            default:
                viewHolder.lastState.setText("Error");
                break;
        }*/

        // Show the right color of bar to the left of the service name
        switch (hosts.get(groupPosition).getServiceState(childPosition)){
            case 1:
                viewHolder.criticalBar.setVisibility(View.GONE);
                viewHolder.warningBar.setVisibility(View.VISIBLE);
                viewHolder.unknownBar.setVisibility(View.GONE);
                break;
            case 2:
                viewHolder.criticalBar.setVisibility(View.VISIBLE);
                viewHolder.warningBar.setVisibility(View.GONE);
                viewHolder.unknownBar.setVisibility(View.GONE);
                break;
            case 3:
                viewHolder.criticalBar.setVisibility(View.GONE);
                viewHolder.warningBar.setVisibility(View.GONE);
                viewHolder.unknownBar.setVisibility(View.VISIBLE);
                break;
            default:
                viewHolder.criticalBar.setVisibility(View.GONE);
                viewHolder.warningBar.setVisibility(View.GONE);
                viewHolder.unknownBar.setVisibility(View.INVISIBLE);
                break;
        }

        if (hosts.get(groupPosition).isServiceExpanded(childPosition)) {
            viewHolder.childExpand.setVisibility(View.VISIBLE);
        } else {
            viewHolder.childExpand.setVisibility(View.GONE);
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
        } else {
            viewHolder.childExpand.setVisibility(View.VISIBLE);
            hosts.get(groupPosition).setServiceExpanded(childPosition, true);
        }
    }


    private class GroupViewHolder{
        final TextView hostName;
        final TextView downHostName;
        final TextView criticalCount;
        final TextView warningCount;
        final TextView unknownCount;


        GroupViewHolder(View view){
            hostName = (TextView) view.findViewById(R.id.main_exp_list_hostname);
            downHostName = (TextView) view.findViewById(R.id.main_exp_list_hostname_down);
            criticalCount = (TextView) view.findViewById(R.id.main_exp_list_critical_count);
            warningCount = (TextView) view.findViewById(R.id.main_exp_list_warning_count);
            unknownCount = (TextView) view.findViewById(R.id.main_exp_list_unknown_count);
        }
    }

    private class ChildViewHolder{
        final TextView serviceName;
        final TextView serviceDetails;
        final TextView criticalBar;
        final TextView warningBar;
        final TextView unknownBar;
        final LinearLayout childExpand;
        final CheckBox serviceNotifications;
        //final TextView lastState;
        //final TextView lastStateChange;


        ChildViewHolder(View view){
            serviceName = (TextView) view.findViewById(R.id.expandedListItem);
            serviceDetails = (TextView) view.findViewById(R.id.exp_service_details);
            criticalBar = (TextView) view.findViewById(R.id.critical_state_bar);
            warningBar = (TextView) view.findViewById(R.id.warning_state_bar);
            unknownBar = (TextView) view.findViewById(R.id.unknown_state_bar);
            childExpand = (LinearLayout) view.findViewById(R.id.child_expand);
            serviceNotifications = (CheckBox) view.findViewById(R.id.service_notifications);
            //lastState = (TextView) view.findViewById(R.id.last_state);
            //lastStateChange = (TextView) view.findViewById(R.id.last_state_change);
        }
    }


}






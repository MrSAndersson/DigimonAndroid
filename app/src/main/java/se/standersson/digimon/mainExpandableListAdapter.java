package se.standersson.digimon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import java.util.List;


class mainExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<Host> hosts;


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
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
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


        ChildViewHolder(View view){
            serviceName = (TextView) view.findViewById(R.id.expandedListItem);
            serviceDetails = (TextView) view.findViewById(R.id.exp_service_details);
            criticalBar = (TextView) view.findViewById(R.id.critical_state_bar);
            warningBar = (TextView) view.findViewById(R.id.warning_state_bar);
            unknownBar = (TextView) view.findViewById(R.id.unknown_state_bar);
        }
    }
}






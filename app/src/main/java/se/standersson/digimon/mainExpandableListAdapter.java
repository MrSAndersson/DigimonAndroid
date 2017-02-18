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

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.main_expanded_list_group, parent, false);
        }


        TextView main_exp_list_header = (TextView)convertView.findViewById(R.id.main_exp_list_hostname);
        //main_exp_list_header.setText(headerTitle);
        TextView main_exp_list_service_count = (TextView) convertView.findViewById(R.id.main_exp_list_service_count);

            /*
            * Show what hosts are down and how many services are down
             */

        main_exp_list_header.setText(hosts.get(groupPosition).getHostName());
        if (hosts.get(groupPosition).isDown()){
            main_exp_list_service_count.setText(R.string.host_down);
        } else {
            String count = Integer.toString(hosts.get(groupPosition).getServiceCount());
            main_exp_list_service_count.setText(count);
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.main_expanded_list_item, parent, false);
        }
        TextView main_exp_list_item = (TextView)convertView.findViewById(R.id.expandedListItem);
        TextView main_exp_list_item_details = (TextView)convertView.findViewById(R.id.exp_service_details);

        String childText = hosts.get(groupPosition).getServiceName(childPosition);
        String details = hosts.get(groupPosition).getServiceDetails(childPosition);

        main_exp_list_item_details.setText(details);
        main_exp_list_item.setText(childText);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}



package se.standersson.digimon;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.RippleDrawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.List;


class mainExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> expandableListGroup;
    private List<String> downedHosts;
    private HashMap<String, List<Integer>> hostServiceCounter;
    private JSONObject data;

    mainExpandableListAdapter(Context context, JSONObject data, List<String> expandableListGroup, HashMap<String, List<Integer>> hostServiceCounter, List<String> downedHosts) {
        this.data = data;
        this.context = context;
        this.expandableListGroup = expandableListGroup;
        this.hostServiceCounter = hostServiceCounter;
        this.downedHosts = downedHosts;
    }

    @Override
    public int getGroupCount() {

        return expandableListGroup.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return hostServiceCounter.get(expandableListGroup.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return expandableListGroup.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        try {
            String host = expandableListGroup.get(groupPosition);
            Integer serviceNr = hostServiceCounter.get(host).get(childPosition);
            return data.getJSONArray("services").getJSONObject(serviceNr).getJSONObject("attrs").getString("name");
        } catch (JSONException e) {
            return "Could not parse child Item";
        }
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
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String)getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.main_expanded_list_group, parent, false);

            TextView main_exp_list_header = (TextView)convertView.findViewById(R.id.main_exp_list_hostname);
            main_exp_list_header.setText(headerTitle);
            TextView main_exp_list_service_count = (TextView) convertView.findViewById(R.id.main_exp_list_service_count);

            /*
            * On all hosts that are down, change the background to red and on others, print the number of services down
             */


            if (downedHosts.contains(headerTitle)) {
                convertView.setBackground(context.getDrawable(R.drawable.host_down_ripple));
                main_exp_list_service_count.setText(R.string.host_down);
            } else {
                String count = Integer.toString(hostServiceCounter.get(expandableListGroup.get(groupPosition)).size());
                main_exp_list_service_count.setText(count);
                convertView.setBackground(ContextCompat.getDrawable(context, R.drawable.host_ripple));
            }
        }


        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String childText = (String)getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.main_expanded_list_item, parent, false);
        }
        TextView main_exp_list_item = (TextView)convertView.findViewById(R.id.expandedListItem);
        main_exp_list_item.setText(childText);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}



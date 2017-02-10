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


public class mainExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> expandableListGroup;
    private List<String> downedHosts;
    private HashMap<String, List<Integer>> hostServiceCounter;
    private JSONObject data;

    public mainExpandableListAdapter(Context context, JSONObject data, List<String> expandableListGroup, HashMap<String, List<Integer>> hostServiceCounter, List<String> downedHosts) {
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
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String)getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.main_expanded_list_group, null);
        }
        TextView main_exp_list_header = (TextView)convertView.findViewById(R.id.main_exp_list_header);
        main_exp_list_header.setText(headerTitle);
        TextView main_exp_list_service_count = (TextView) convertView.findViewById(R.id.main_exp_list_service_count);

        if (downedHosts.contains(headerTitle)) {
            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorHostDown));
            convertView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // TODO Auto-generated method stub
                    switch(event.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                            v.setBackgroundColor(ContextCompat.getColor(context, R.color.colorHostDownPressed));
                            break;
                        case MotionEvent.ACTION_UP:

                            //set color back to default
                            v.setBackgroundColor(ContextCompat.getColor(context, R.color.colorHostDown));
                            break;
                        case MotionEvent.ACTION_OUTSIDE:
                            //set color back to default
                            v.setBackgroundColor(ContextCompat.getColor(context, R.color.colorHostDown));
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            //set color back to default
                            v.setBackgroundColor(ContextCompat.getColor(context, R.color.colorHostDown));
                            break;

                    }
                    return true;
                }
            });
            main_exp_list_service_count.setVisibility(View.GONE);
        } else {
            String count = Integer.toString(hostServiceCounter.get(expandableListGroup.get(groupPosition)).size());
            main_exp_list_service_count.setText(count);
            main_exp_list_service_count.setVisibility(View.VISIBLE);
            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String childText = (String)getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.main_expanded_list_item, null);
        }
        TextView main_exp_list_item = (TextView)convertView.findViewById(R.id.expandedListItem);
        main_exp_list_item.setText(childText);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    /*
    private Context context;
    private List<String> listDataGroup;
    private HashMap<String, List<String>> listHashMap;

    public mainExpandableListAdapter(Context context, List<String> listDataGroup, HashMap<String, List<String>> listHashMap) {
        this.context = context;
        this.listDataGroup = listDataGroup;
        this.listHashMap = listHashMap;
    }

    @Override
    public int getGroupCount() {

        return listDataGroup.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return listHashMap.get(listDataGroup.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listDataGroup.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return listHashMap.get(listDataGroup.get(groupPosition)).get(childPosition);
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
        String headerTitle = (String)getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflator = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflator.inflate(R.layout.main_expanded_list_group, null);
        }
        TextView main_exp_list_header = (TextView)convertView.findViewById(R.id.main_exp_list_header);
        main_exp_list_header.setText(headerTitle);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String childText = (String)getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.main_expanded_list_item, null);
        }
        TextView main_exp_list_item = (TextView)convertView.findViewById(R.id.expandedListItem);
        main_exp_list_item.setText(childText);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }*/
}



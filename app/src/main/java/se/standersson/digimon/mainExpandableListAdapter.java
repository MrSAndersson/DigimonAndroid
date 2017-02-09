package se.standersson.digimon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import java.util.HashMap;
import java.util.List;


public class mainExpandableListAdapter extends BaseExpandableListAdapter {
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
    }
}



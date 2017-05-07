package se.standersson.icingalert;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.List;

public class HostListFragment extends Fragment {
    private SwipeRefreshLayout swipeContainer;
    private View view;
    private Context parentActivity;
    private List<Host> hosts;

    static HostListFragment newInstance(int position, List<Host> hosts) {
        HostListFragment fragment = new HostListFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putSerializable("hosts", (Serializable) hosts);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = getActivity();
        // noinspection unchecked
        hosts = (List<Host>) getArguments().getSerializable("hosts");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_problem, container, false);

        /*
         * Set up a callback for refresh PullDown
         */
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.exp_swipe);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ((MainActivity)parentActivity).refresh();
            }
        });

        ExpandableListView listView = (ExpandableListView) view.findViewById(R.id.main_expand_list);
        final mainExpandableListAdapter listAdapter = new mainExpandableListAdapter(view.getContext(), hosts);
        listView.setAdapter(listAdapter);
        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
                listAdapter.childClick(groupPosition, childPosition, view);
                return true;
            }
        });

        return view;
    }


    public void setRefreshSpinner(boolean run){
        if (run) {
            swipeContainer.setRefreshing(true);
        } else {
            swipeContainer.setRefreshing(false);
        }
    }

    public void update(List<Host> hosts){
        ExpandableListView listView = (ExpandableListView) view.findViewById(R.id.main_expand_list);
        ExpandableListAdapter listAdapter = new mainExpandableListAdapter(view.getContext(), hosts);
        listView.setAdapter(listAdapter);
    }

}

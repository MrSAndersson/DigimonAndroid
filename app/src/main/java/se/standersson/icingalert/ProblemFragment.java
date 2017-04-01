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

public class ProblemFragment extends Fragment {
    private SwipeRefreshLayout swipeContainer;
    private View view;
    private int hostsNr;
    private boolean isTroubleList;
    private Context parentActivity;

    static ProblemFragment newInstance(int position, int hostsNr, boolean isTroubleList) {
        ProblemFragment fragment = new ProblemFragment();
        Bundle args = new Bundle();
        args.putInt("hostsNr", hostsNr);
        args.putInt("position", position);
        args.putBoolean("isTroubleList", isTroubleList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hostsNr = getArguments().getInt("hostsNr", 0);
        isTroubleList = getArguments().getBoolean("isTroubleList", false);
        parentActivity = getActivity();
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
        ExpandableListAdapter listAdapter = new mainExpandableListAdapter(view.getContext(), MainActivity.hosts, hostsNr, isTroubleList);
        listView.setAdapter(listAdapter);

        return view;
    }


    public void setRefreshSpinner(boolean run){
        if (run) {
            swipeContainer.setRefreshing(true);
        } else {
            swipeContainer.setRefreshing(false);
        }
    }

    public void update(int hostsNr){
        ExpandableListView listView = (ExpandableListView) view.findViewById(R.id.main_expand_list);
        ExpandableListAdapter listAdapter = new mainExpandableListAdapter(view.getContext(), MainActivity.hosts, hostsNr, isTroubleList);
        listView.setAdapter(listAdapter);
    }

}

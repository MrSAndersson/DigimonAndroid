package se.standersson.icingalert;


import android.graphics.drawable.TransitionDrawable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.io.Serializable;
import java.util.List;

public class HostListFragment extends Fragment implements MainDataReceived{
    private HostListFragment thisClass = this;
    private SwipeRefreshLayout swipeContainer;
    private View view;
    private MainPagerAdapter mainPagerAdapter;
    private List<HostList> hosts;
    private boolean backgroundIsBlue = false;
    private int fragmentPosition;
    private int globalProblemHostCount;

    static HostListFragment newInstance(int position, List<HostList> hosts) {
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
        // noinspection unchecked
        this.hosts = (List<HostList>) getArguments().getSerializable("hosts");
        this.fragmentPosition = getArguments().getInt("position");
        this.globalProblemHostCount = Tools.filterProblems(HostSingleton.getInstance().getHosts()).size();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_problem, container, false);

        mainPagerAdapter = ((MainActivity)getActivity()).getMainPagerAdapter();

        /*
         * Set up a callback for refresh PullDown
         */
        swipeContainer = view.findViewById(R.id.exp_swipe);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Show the update spinners on the main ExpandableListViews
                mainPagerAdapter.getFragment(1).setRefreshSpinner(true);
                mainPagerAdapter.getFragment(0).setRefreshSpinner(true);
                new MainDataFetch((MainActivity)getActivity()).refresh(thisClass);
            }
        });

        ExpandableListView listView = view.findViewById(R.id.main_expand_list);
        final mainExpandableListAdapter listAdapter = new mainExpandableListAdapter(view.getContext(), hosts);
        listView.setAdapter(listAdapter);

        // If list is empty, display All Clear
        TransitionDrawable backgroundTransition = (TransitionDrawable) view.getBackground();

        if (globalProblemHostCount == 0 && fragmentPosition == 0) {
            listView.setVisibility(View.GONE);
            view.findViewById(R.id.main_expand_all_clear).setVisibility(View.VISIBLE);
            if (!backgroundIsBlue) {
                backgroundTransition.startTransition(180);
                backgroundIsBlue = true;
            }
        } else {
            listView.setVisibility(View.VISIBLE);
            view.findViewById(R.id.main_expand_all_clear).setVisibility(View.GONE);
            if (backgroundIsBlue) {
                backgroundTransition.reverseTransition(180);
                backgroundIsBlue = false;
            }
        }

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

    public void update(List<HostList> hosts, boolean collapse){
        this.globalProblemHostCount = hosts.size();
        ExpandableListView listView = view.findViewById(R.id.main_expand_list);
        mainExpandableListAdapter adapter = (mainExpandableListAdapter) listView.getExpandableListAdapter();
        adapter.updateHostList(hosts);
        adapter.notifyDataSetChanged();

        // If list is empty, display All Clear
        TransitionDrawable backgroundTransition = (TransitionDrawable) view.getBackground();
        if (globalProblemHostCount == 0 && fragmentPosition == 0) {
            listView.setVisibility(View.GONE);
            view.findViewById(R.id.main_expand_all_clear).setVisibility(View.VISIBLE);
            if (!backgroundIsBlue) {
                backgroundTransition.startTransition(180);
                backgroundIsBlue = true;
            }
        } else {
            listView.setVisibility(View.VISIBLE);
            view.findViewById(R.id.main_expand_all_clear).setVisibility(View.GONE);
            if (backgroundIsBlue) {
                backgroundTransition.reverseTransition(180);
                backgroundIsBlue = false;
            }
        }

        if (collapse) {
            //Collapse all groups
            for (int x = 0; x < hosts.size(); x++) {
                listView.collapseGroup(x);
            }
        }
    }

    @Override
    public void mainDataReceived(boolean success) {
        // Remove the update spinners from the main ExpandableListViews
        mainPagerAdapter.getFragment(0).setRefreshSpinner(false);
        mainPagerAdapter.getFragment(1).setRefreshSpinner(false);

        if (success) {
            mainPagerAdapter.getFragment(0).update(Tools.filterProblems(HostSingleton.getInstance().getHosts()), true);
            mainPagerAdapter.getFragment(1).update(Tools.fullHostList(HostSingleton.getInstance().getHosts()), true);
        }
    }
}

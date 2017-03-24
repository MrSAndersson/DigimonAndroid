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
    View view;
    int hostsNr;
    private Context parentActivity;

    static ProblemFragment newInstance(int hostsNr) {
        ProblemFragment fragment = new ProblemFragment();
        Bundle args = new Bundle();
        args.putInt("hostsNr", hostsNr);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
       // outState.putString("reply", reply);
        super.onSaveInstanceState(outState);
    }

    /*public ProblemFragment() {

    }*/

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hostsNr = getArguments().getInt("hostsNr", 0);
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
        ExpandableListAdapter listAdapter = new mainExpandableListAdapter(view.getContext(), MainActivity.hosts, hostsNr);
        listView.setAdapter(listAdapter);

        return view;
    }


    public void stopRefreshSpinner(boolean stop){
        if (stop) {
            swipeContainer.setRefreshing(false);
        }
    }

}

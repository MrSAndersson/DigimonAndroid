package se.standersson.icingalert;


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

    static ProblemFragment newInstance(int num) {
        return new ProblemFragment();
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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_problem, container, false);

        /*
         * Set up a callback for refresh PullDown
         */
        /*swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.exp_swipe);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() { ((se.standersson.icingalert.MainActivity)getActivity());}
        });*/

        ExpandableListView listView = (ExpandableListView) view.findViewById(R.id.main_expand_list);
        ExpandableListAdapter listAdapter = new mainExpandableListAdapter(view.getContext(), MainActivity.hosts);
        listView.setAdapter(listAdapter);

        return view;
    }

}

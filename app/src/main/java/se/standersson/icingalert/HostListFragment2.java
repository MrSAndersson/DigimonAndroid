package se.standersson.icingalert;

import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;
import java.util.List;

public class HostListFragment2 extends Fragment implements MainDataReceived{

    private OnListFragmentInteractionListener mListener;
    private HostListFragment2 thisClass = this;
    private List<HostList> hosts;
    private SwipeRefreshLayout swipeContainer;
    private MainPagerAdapter mainPagerAdapter;
    private View view;
    private boolean backgroundIsBlue = false;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HostListFragment2() {
    }

    @SuppressWarnings("unused")
    public static HostListFragment2 newInstance(int position, List<HostList> hosts) {
        HostListFragment2 fragment = new HostListFragment2();
        Bundle args = new Bundle();
        args.putSerializable("hosts", (Serializable) hosts);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // noinspection unchecked
            this.hosts = (List<HostList>) getArguments().getSerializable("hosts");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_hostlist_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.main_list);

        mainPagerAdapter = ((MainActivity)getActivity()).getMainPagerAdapter();

        /*
         * Set up a callback for refresh PullDown
         */
        swipeContainer = view.findViewById(R.id.main_list_swipe);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Show the update spinners on the main ExpandableListViews
                mainPagerAdapter.getFragment2(0).setRefreshSpinner(true);
                mainPagerAdapter.getFragment2(1).setRefreshSpinner(true);
                new MainDataFetch((MainActivity)getActivity()).refresh(thisClass);
            }
        });

        // If list is empty, display All Clear
        TransitionDrawable backgroundTransition = (TransitionDrawable) view.getBackground();

        if (isAllClear()) {
            recyclerView.setVisibility(View.GONE);
            view.findViewById(R.id.main_list_all_clear).setVisibility(View.VISIBLE);
            if (!backgroundIsBlue) {
                backgroundTransition.startTransition(180);
                backgroundIsBlue = true;
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            view.findViewById(R.id.main_list_all_clear).setVisibility(View.GONE);
            if (backgroundIsBlue) {
                backgroundTransition.reverseTransition(180);
                backgroundIsBlue = false;
            }
        }

        // Set the adapter
        Context context = view.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new MyHostListRecyclerViewAdapter(context, hosts, mListener));
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(HostList host);
    }

    @Override
    public void mainDataReceived(boolean success) {
        // Remove the update spinners from the main ExpandableListViews
        mainPagerAdapter.getFragment2(0).setRefreshSpinner(false);
        mainPagerAdapter.getFragment2(1).setRefreshSpinner(false);

        if (success) {
            mainPagerAdapter.getFragment2(0).update(Tools.filterProblems(HostSingleton.getInstance().getHosts()));
            mainPagerAdapter.getFragment2(1).update(Tools.fullHostList(HostSingleton.getInstance().getHosts()));
        }
    }

    public void setRefreshSpinner(boolean run){
        if (run) {
            swipeContainer.setRefreshing(true);
        } else {
            swipeContainer.setRefreshing(false);
        }
    }

    public void update(List<HostList> hosts){

        // Update RecyclerView with the latest data and tell it to update itself
        RecyclerView recyclerView = view.findViewById(R.id.main_list);
        MyHostListRecyclerViewAdapter adapter = (MyHostListRecyclerViewAdapter) recyclerView.getAdapter();
        adapter.updateHostList(hosts);
        adapter.notifyDataSetChanged();

        // If list is empty, display All Clear
        TransitionDrawable backgroundTransition = (TransitionDrawable) view.getBackground();

        if (isAllClear()) {
            recyclerView.setVisibility(View.GONE);
            view.findViewById(R.id.main_list_all_clear).setVisibility(View.VISIBLE);
            if (!backgroundIsBlue) {
                backgroundTransition.startTransition(180);
                backgroundIsBlue = true;
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            view.findViewById(R.id.main_list_all_clear).setVisibility(View.GONE);
            if (backgroundIsBlue) {
                backgroundTransition.reverseTransition(180);
                backgroundIsBlue = false;
            }
        }
    }

    boolean isAllClear() {
        if ( HostSingleton.getInstance().getProblemHostCount() == 0 && hosts.size() == 0) {
            return true;
        } else {
            return false;
        }
    }
}

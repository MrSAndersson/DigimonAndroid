package se.standersson.icingalert;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

class MainPagerAdapter extends FragmentPagerAdapter {
    private static final int TAB_COUNT = 4;
    private final HostListFragment[] fragmentArray = new HostListFragment[2];
    private final HostListFragment2[] fragmentArray2 = new HostListFragment2[2];

    MainPagerAdapter(android.support.v4.app.FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return TAB_COUNT;
    }

    // Returns the fragment to display for that page
    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        // Get the number of hosts with any kind of problem
        int globalProblemHostCount = Tools.filterProblems(HostSingleton.getInstance().getHosts()).size();
        
        switch (position) {
            case 0: // Trouble List
                HostListFragment troubleFragment = HostListFragment.newInstance(position, Tools.filterProblems(HostSingleton.getInstance().getHosts()), globalProblemHostCount);
                fragmentArray[0] = troubleFragment;
                return troubleFragment;
            case 1: // All-things-list
                HostListFragment allFragment = HostListFragment.newInstance(position, Tools.fullHostList(HostSingleton.getInstance().getHosts()), globalProblemHostCount);
                fragmentArray[1] = allFragment;
                return allFragment;
            case 2:
                HostListFragment2 testTroubleFragment = HostListFragment2.newInstance(position, Tools.filterProblems(HostSingleton.getInstance().getHosts()));
                fragmentArray2[0] = testTroubleFragment;
                return testTroubleFragment;
            case 3:
                HostListFragment2 testAllFragment = HostListFragment2.newInstance(position, Tools.fullHostList(HostSingleton.getInstance().getHosts()));
                fragmentArray2[1] = testAllFragment;
                return testAllFragment;
            default:
                return null;
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (position < 2) {
            HostListFragment fragment = (HostListFragment) super.instantiateItem(container, position);
            fragmentArray[position] = fragment;
            return fragment;
        }

        HostListFragment2 fragment2 = (HostListFragment2) super.instantiateItem(container, position);
        fragmentArray2[position - 2] = fragment2;
        return fragment2;

    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Trouble";
            case 1:
                return "All";
            case 2:
                return "NewTrouble";
            case 3:
                return "NewAll";
            default:
                return "Wrong";
        }
    }

    HostListFragment getFragment(int position){
        return fragmentArray[position];
    }

    HostListFragment2 getFragment2(int position) {
        return fragmentArray2[position];
    }
}
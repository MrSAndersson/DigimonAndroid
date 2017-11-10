package se.standersson.icingalert;

import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

class MainPagerAdapter extends FragmentPagerAdapter {
    private static final int NUM_ITEMS = 2;
    private final HostListFragment[] fragmentArray = new HostListFragment[2];

    MainPagerAdapter(android.support.v4.app.FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return NUM_ITEMS;
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
            default:
                return null;
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        HostListFragment fragment = (HostListFragment) super.instantiateItem(container, position);
        fragmentArray[position] = fragment;
        return fragment;
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Trouble";
            case 1:
                return "All";
            default:
                return "Wrong";
        }
    }

    HostListFragment getFragment(int position){
        return fragmentArray[position];
    }
}
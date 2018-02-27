package se.standersson.icingalert;

import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

class MainPagerAdapter extends FragmentPagerAdapter {
    private static final int TAB_COUNT = 2;
    private final HostListFragment[] fragmentArray2 = new HostListFragment[2];

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
        switch (position) {
            case 0: // Trouble List
                HostListFragment testTroubleFragment = HostListFragment.newInstance(position, Tools.filterProblems(HostSingleton.getInstance().getHosts()));
                fragmentArray2[0] = testTroubleFragment;
                return testTroubleFragment;
            case 1:  // All-things-list
                HostListFragment testAllFragment = HostListFragment.newInstance(position, Tools.fullHostList(HostSingleton.getInstance().getHosts()));
                fragmentArray2[1] = testAllFragment;
                return testAllFragment;
            default:
                return null;
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        HostListFragment fragment = (HostListFragment) super.instantiateItem(container, position);
        fragmentArray2[position] = fragment;
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

    HostListFragment getFragment2(int position) {
        return fragmentArray2[position];
    }
}
package com.dleibovych.epictale.fragment.dialog;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dleibovych.epictale.DataViewMode;
import com.dleibovych.epictale.R;
import com.dleibovych.epictale.widget.WrappingViewPager;

/**
 * @author Hamster
 * @since 15.10.2014
 */
public class TabbedDialog extends BaseDialog {

    private static final String PARAM_CAPTION = "PARAM_CAPTION";
    private TabbedDialogTabsAdapter tabsAdapter = null;
    private PagerAdapter pagerAdapter;
    private boolean isTabsInitialized = true;

    public static TabbedDialog newInstance(final String caption) {
        final TabbedDialog dialog = new TabbedDialog();

        Bundle args = new Bundle();
        args.putString(PARAM_CAPTION, caption);

        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog_content_tabbed, container, false);
        final View dialogView = wrapView(inflater, view, getArguments().getString(PARAM_CAPTION));

        isTabsInitialized = false;
        if(tabsAdapter == null) {
            setMode(DataViewMode.LOADING);
        }

        return dialogView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(!isTabsInitialized && (tabsAdapter != null)) {
            setupTabs();
        }
    }

    public void setTabsAdapter(final TabbedDialogTabsAdapter tabsAdapter) {
        this.tabsAdapter = tabsAdapter;
        if(!isTabsInitialized) {
            isTabsInitialized = true;
            setupTabs();
        }
    }

    private void setupTabs() {
        final WrappingViewPager viewPager = getView().findViewById(R.id.dialog_tabbed_pager);
        pagerAdapter = new TabbedDialogPagerAdapter(tabsAdapter);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(pagerAdapter.getCount());
    }

    public static abstract class TabbedDialogTabsAdapter {

        public abstract int getCount();

        public abstract Fragment getItem(int i);

        public CharSequence getPageTitle(int position) {
            return null;
        }

    }

    private class TabbedDialogPagerAdapter extends FragmentStatePagerAdapter {

        private final TabbedDialogTabsAdapter tabsAdapter;

        public TabbedDialogPagerAdapter(final TabbedDialogTabsAdapter tabsAdapter) {
            super(getChildFragmentManager());
            this.tabsAdapter = tabsAdapter;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabsAdapter.getPageTitle(position);
        }

        @Override
        public Fragment getItem(int i) {
            return tabsAdapter.getItem(i);
        }

        @Override
        public int getCount() {
            return tabsAdapter.getCount();
        }

    }

}

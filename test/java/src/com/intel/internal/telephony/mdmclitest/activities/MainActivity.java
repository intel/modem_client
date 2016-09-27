/*
 * Copyright (C) 2015 Intel Corporation, All rights Reserved.
 *
 * Modem Client library has been designed by:
 *  - Cesar De Oliveira <cesar.de.oliveira@intel.com>
 *  - Lionel Ulmer <lionel.ulmer@intel.com>
 *  - Marc Bellanger <marc.bellanger@intel.com>
 *
 * Original contributor for the test app is:
 *  - Edward Marmounier <edward.marmounier@intel.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.internal.telephony.mdmclitest.activities;

import com.intel.internal.telephony.mdmclitest.R;
import com.intel.internal.telephony.mdmclitest.adapters.OperationTitleAdapter;
import com.intel.internal.telephony.mdmclitest.adapters.SectionsPagerAdapter;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.Menu;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
    private final Boolean isDSDA =
        SystemProperties.get("persist.radio.multisim.config", "").contains("dsda");
    private OperationTitleAdapter operationTitleAdapter;
    private ViewPager viewPager;
    private PagerTabStrip pagerTab;
    private List<PageComponent> pageComponentList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.main);
        // Create the adapter that will return a fragment for each
        // primary sections of the app. like 'Modem Management BASIC OPS'
        operationTitleAdapter = new OperationTitleAdapter(
            super.getSupportFragmentManager(), this);

        // Set up the action bar.
        final ActionBar actionBar = super.getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager with the sections adapter.
        this.viewPager = (ViewPager) super.findViewById(R.id.pager);

        if (this.viewPager == null) {
            return;
        }

        String[] tabInfo = new String[0];

        if (isDSDA) {
            tabInfo = getResources().getStringArray(R.array.tab_name);
        } else {
            viewPager.removeView(findViewById(R.id.pagertab));
        }

        pageComponentList = new ArrayList<PageComponent>();

        // Now we have two Action Bars: OperationTitle & SectionsPager
        // Delete the Page change listener of OperationTitle,
        // the swaping of SectionsPager only will be enabled.

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < operationTitleAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter.
            // Also specify this Activity object, which implements the
            // TabListener interface, as the
            // listener for when this tab is selected.
            CharSequence pageTitleName = operationTitleAdapter.getPageTitle(i);

            pageComponentList.add(new PageComponent(pageTitleName,
                                                    new SectionsPagerAdapter(
                                                        super.getSupportFragmentManager(),
                                                        tabInfo)));

            actionBar.addTab(actionBar.newTab()
                             .setText(pageTitleName)
                             .setTabListener(this));
        }

        PageComponent firstPage = pageComponentList.get(0);
        if ((null != firstPage)) {
            this.viewPager.setAdapter(firstPage.getFragmentPagerAdapter());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab,
                                FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab,
                              FragmentTransaction fragmentTransaction) {
        int position = tab.getPosition();

        if (pageComponentList.size() > 1) {
            viewPager.setAdapter(pageComponentList.get(position).getFragmentPagerAdapter());
        }

        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        this.viewPager.setCurrentItem(position);
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab,
                                FragmentTransaction fragmentTransaction) {
    }

    class PageComponent {
        private CharSequence pageTitleName;
        private FragmentPagerAdapter pagerAdapter;

        public PageComponent(CharSequence pageTitleName,
                             FragmentPagerAdapter pagerAdapter) {
            this.pageTitleName = pageTitleName;
            this.pagerAdapter = pagerAdapter;
        }

        public CharSequence getPageTitleName() {
            return pageTitleName;
        }

        public FragmentPagerAdapter getFragmentPagerAdapter() {
            return pagerAdapter;
        }
    }
}

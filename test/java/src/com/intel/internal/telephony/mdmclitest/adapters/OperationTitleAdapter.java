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

package com.intel.internal.telephony.mdmclitest.adapters;

import com.intel.internal.telephony.mdmclitest.fragments.ModemStatusFragment;
import com.intel.internal.telephony.mdmclitest.R;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one
 * of the primary sections of the app.
 */
public class OperationTitleAdapter extends FragmentPagerAdapter {
    private final String TAG = "OperationTitleAdapter";
    private String[] titleSection;

    public OperationTitleAdapter(FragmentManager fm, Context context) {
        super(fm);
        titleSection = context.getResources().getStringArray(R.array.title_section);
    }

    @Override
    public Fragment getItem(int i) {
        return null;
    }

    @Override
    public int getCount() {
        return titleSection.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String titleName = null;

        try {
            titleName = titleSection[position];
        } catch (ArrayIndexOutOfBoundsException ex) {
            Log.e(TAG, "position ERROR " + ex);
        }

        return titleName;
    }
}

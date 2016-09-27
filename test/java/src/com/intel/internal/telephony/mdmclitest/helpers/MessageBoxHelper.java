/*
 * Copyright (C) Intel 2012
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

package com.intel.internal.telephony.mdmclitest.helpers;

import com.intel.internal.telephony.mdmclitest.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class MessageBoxHelper {
    public static void showException(Context context, Exception ex) {
        new AlertDialog.Builder(context).setTitle("Error")
        .setMessage(ex.getMessage())
        .setPositiveButton("Ok", new OnClickListener() {
                               public void onClick(DialogInterface arg0, int arg1) {
                                   arg0.dismiss();
                               }
                           }).show();
    }

    public static void showMessage(Context context, String msg) {
        new AlertDialog.Builder(context)
        .setTitle(context.getResources().getString(R.string.app_name))
        .setMessage(msg).setPositiveButton("Ok", new OnClickListener() {
                                               public void onClick(DialogInterface arg0, int arg1) {
                                                   arg0.dismiss();
                                               }
                                           }).show();
    }
}

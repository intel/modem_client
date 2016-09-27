/*
 *
 * Copyright (C) Intel 2015
 *
 * Modem Client library has been designed by:
 *  - Cesar De Oliveira <cesar.de.oliveira@intel.com>
 *  - Lionel Ulmer <lionel.ulmer@intel.com>
 *  - Marc Bellanger <marc.bellanger@intel.com>
 *
 * Original contributors are:
 *  - Cesar De Oliveira <cesar.de.oliveira@intel.com>
 *  - Lionel Ulmer <lionel.ulmer@intel.com>
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
 *
 */

/**
 * The aim of this file is just to provide a simple example of Java client API usage
 */
import android.content.Context;
import android.util.Log;

import com.intel.internal.telephony.ModemClientException;
import com.intel.internal.telephony.ModemEventListener;
import com.intel.internal.telephony.ModemStatus;
import com.intel.internal.telephony.ModemStatusManager;

public class ExampleModemHandling implements ModemEventListener {
    private final String TAG = "<my tag>";
    private ModemStatusManager modemStatusManager;

    private ExampleModemHandling(Context context, int instanceId) {
        try {
            this.modemStatusManager = ModemStatusManager.getInstance(context,
                                                                     instanceId);
        } catch (InstantiationException ex) {
            Log.e(TAG, "Cannot instantiate Modem Status Manager");
        }
    }

    /**
     * This function registers to modem management system (by subscribing to all events) and
     * acquires the modem resource.
     */
    private void connectToModem() {
        if (this.modemStatusManager != null) {
            try {
                Log.d(TAG, MODULE + ": Subscribing to Modem Status Manager");
                this.modemStatusManager.subscribeToEvent(this, ModemStatus.ALL);
            } catch (ModemClientException ex) {
                Log.e(TAG, "Cannot subscribe to Modem Status Manager " + ex);
            }
            try {
                Log.d(TAG, MODULE + ": Connecting to Modem Status Manager");
                this.modemStatusManager.connect("<Client name>");
            } catch (ModemClientException ex) {
                Log.e(TAG, "Cannot connect to Modem Status Manager " + ex);
            }
            try {
                Log.d(TAG, MODULE + ": Acquiring modem resource");
                this.modemStatusManager.acquireModem();
            } catch (ModemClientException ex) {
                Log.e(TAG, "Cannot acquire modem resource " + ex);
            }
        }
    }

    /**
     * Disconnects from modem management system
     */
    private void disconnectToModem() {
        if (this.modemStatusManager != null) {
            this.modemStatusManager.disconnect();
            this.modemStatusManager = null;
        }
    }


    // Callbacks:
    @Override
    public void onModemUp() {
        // handle modem UP event here
    }

    @Override
    public void onModemDown() {
        // handle modem DOWN event here
    }

    @Override
    public void onModemDead() {
        // handle modem OUT OF SERVICE event here
    }
}

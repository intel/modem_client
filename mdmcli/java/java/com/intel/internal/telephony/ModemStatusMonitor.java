/*
 * Copyright (C) Intel 2012
 *
 * Modem Client library has been designed by:
 *  - Cesar De Oliveira <cesar.de.oliveira@intel.com>
 *  - Lionel Ulmer <lionel.ulmer@intel.com>
 *  - Marc Bellanger <marc.bellanger@intel.com>
 *
 * Original contributors are:
 *  - Cesar De Oliveira <cesar.de.oliveira@intel.com>
 *  - Edward Marmounier <edward.marmounier@intel.com>
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
 */

package com.intel.internal.telephony;

import android.os.Message;
import android.os.Handler.Callback;

public interface ModemStatusMonitor extends Callback {
    /* Communication error is detected */
    public static final int MSG_ERROR = 1;
    /* An event is received */
    public static final int MSG_STATUS = 2;

    /**
     * Connects to Modem Management system.
     * Client provides his name
     *
     * @param [in] clientName Name of the client
     * @param [in] instanceId Modem instance ID
     *
     * @throws ModemClientException
     */
    public void connect(String clientName, int instanceId) throws ModemClientException;

    /**
     * Disconnects to Modem Management system.
     */
    public void disconnect();

    /**
     * Acquires the modem resource.
     * This function must send a modem acquire request to the modem management system.
     *
     * @throws ModemClientException
     */
    public void acquireModem() throws ModemClientException;

    /**
     * Releases the modem resource.
     * This function must send a modem release request to the modem management system.
     *
     * @throws ModemClientException
     */
    public void releaseModem() throws ModemClientException;

    /**
     * Resets the modem.
     * This function must send a modem reset request to the modem management system.
     *
     * @param [in] causes Array of String describing the reason. Will be reported to crashtool
     * @param [in] apLogsSize Size of ap logs to be attached in MBytes,
     *              -1: default, 0: logs not requested
     * @param [in] bpLogsSize Size of bp logs to be attached in MBytes,
     *              -1: default, 0: logs not requested
     * @param [in] bpLogsTime Time of the logs to attach in seconds,
     *              -1: default, 0: logs not requested
     *
     * @throws ModemClientException
     */
    public void resetModem(String[] causes, int apLogsSize, int bpLogsSize, int bpLogsTime)
    throws ModemClientException;

    /**
     * Updates the modem.
     * This function must send a modem update request to the modem management system.
     *
     * @throws ModemClientException
     */
    public void updateModem()
    throws ModemClientException;

    /**
     * Notifies a debug info to modem management system.
     *
     * @param [in] causes Array of String describing the reason. Will be reported to crashtool
     * @param [in] type Type of event (DebugInfoType)
     * @param [in] apLogsSize Size of ap logs to be attached in MBytes,
     *              -1: default, 0: logs not requested
     * @param [in] bpLogsSize Size of bp logs to be attached in MBytes,
     *              -1: default, 0: logs not requested
     * @param [in] bpLogsTime Time of the logs to attach in seconds,
     *              -1: default, 0: logs not requested
     * @throws ModemClientException
     */
    public void notifyDebugInfo(String[] causes, int type, int apLogsSize, int bpLogsSize,
                                int bpLogsTime) throws ModemClientException;

    /**
     * Shutdowns the modem. This function forces a modem shutdown even if client(s)
     * hold the resource.
     * This function must send a modem shutdown request to the modem management system.
     *
     * @throws ModemClientException
     */
    public void shutdownModem() throws ModemClientException;

    /**
     * Waits for a modem status
     *
     * @param [in] status The modem status to wait for
     * @param [in] The maximum amount of time (in milliseconds) to wait
     *
     * @return True if the status was received; otherwise False
     *
     * @throws ModemClientException
     */
    public boolean waitForModemStatus(ModemStatus status, long timeout) throws ModemClientException;

    @Override
    public boolean handleMessage(Message msg);
}

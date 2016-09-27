/*
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
 */

package com.intel.internal.telephony;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Object;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JniModemStatusMonitor implements ModemStatusMonitor {
    private static final String LOG_TAG = "MDMCLI_JNI";
    private static Handler mHandler = null;

    /* Load mdmcli JNI */
    static {
        System.loadLibrary("mdmcli_jni");
    }

    /* JNI functions: */
    private native void jni_connect(String clientName, int intanceId);
    private native void jni_disconnect();

    private native int jni_resetModem(String[] causes, int apLogsSize, int bpLogsSize, int bpLogsTimeint);
    private native int jni_updateModem();
    private native int jni_notifyDebugInfo(String[] causes, int type, int apLogsSize, int bpLogsSize, int bpLogsTime);

    private native int jni_shutdownModem();

    private native int jni_acquireModem();
    private native int jni_releaseModem();

    public JniModemStatusMonitor(Handler handler) {
        mHandler = handler;
    }

    @Override
    public void connect(String clientName, int instanceId) throws ModemClientException {
        Log.d(LOG_TAG, "Connecting client...");
        jni_connect(clientName, instanceId);
        Log.d(LOG_TAG, "Client connected");
    }

    @Override
    public void disconnect() {
        Log.d(LOG_TAG, "Stopping client...");
        jni_disconnect();
        Log.d(LOG_TAG, "Client stopped");
    }

    @Override
    public void acquireModem() throws ModemClientException {
        jni_acquireModem();
    }

    @Override
    public void releaseModem() throws ModemClientException {
        jni_releaseModem();
    }

    @Override
    public void resetModem(String[] causes, int apLogsSize, int bpLogsSize,
                           int bpLogsTime) throws ModemClientException {
        jni_resetModem(causes, apLogsSize, bpLogsSize, bpLogsTime);
    }

    @Override
    public void updateModem() throws ModemClientException {
        jni_updateModem();
    }

    @Override
    public void notifyDebugInfo(String[] causes, int type, int apLogsSize, int bpLogsSize,
                                int bpLogsTime) throws ModemClientException {
        jni_notifyDebugInfo(causes, type, apLogsSize, bpLogsSize, bpLogsTime);
    }

    @Override
    public void shutdownModem() throws ModemClientException {
        jni_shutdownModem();
    }

    @Override
    public boolean waitForModemStatus(ModemStatus status, long timeout) {
        return false;
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }

    private static void callback(int state) {
        if (mHandler != null) {
            mHandler.obtainMessage(ModemStatusMonitor.MSG_STATUS, state).sendToTarget();
        }
    }
}

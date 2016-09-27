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

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Constructor;

import android.content.Context;
import android.content.ContextWrapper;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;

/**
 * Implements the client side that abstracts communication to the Modem Status
 * Monitor service.
 *
 * This class is singleton, you must instantiate it through getInstance() method.
 *
 * The usage of this API is as follow:
 * - Implement the ModemEventListener interface
 * - Instantiate ModemStatusManager
 * - Pass your implementation of ModemEventListener to ModemStatusManager.subscribeToEvent() method
 * - Also pass the bit mask of events you wish to listen to
 * - Call ModemStatusManager.connect() when your app starts
 * - Call ModemStatusManager.disconnect() when your app stops
 */
public class ModemStatusManager implements Callback {
    private static final String TAG = "ModemStatusManager";
    private ModemEventListener eventListener = null;


    /* Those values must be aligned with mdm_cli.h */
    private final static int MDM_DOWN = 1;
    private final static int MDM_UP = 3;
    private final static int MDM_DEAD = 4;

    private ModemStatusMonitor modemStatusMonitor = null;
    private Handler statusEventsHandler = null; // Client <- Modem Management
    private Handler requestHandler = null;      // Client -> Modem Management
    private Integer mInstanceId = Constants.DEFAULT_INSTANCE;

    private static Map<Integer, ModemStatusManager> sInstances = new HashMap<Integer, ModemStatusManager>();

    private ModemStatusManager(Context context, int inst) throws InstantiationException {
        // Context is now useless

        this.mInstanceId = inst;
        this.statusEventsHandler = new Handler(this);
        this.modemStatusMonitor = new JniModemStatusMonitor(this.statusEventsHandler);
        if (this.modemStatusMonitor == null) {
            throw new InstantiationException("Not able to load the modemStatusMonitor");
        }
        this.requestHandler = new Handler(this.modemStatusMonitor);
    }

    /**
     * Returns the instance of ModemStatusManager matching the instanceId
     *
     * @param instanceId specify on which Modem Management instance the client is to be connected
     *                   to connect to first instance of Modem Management instanceId must be equal to 1
     * @return The instance of ModemStatusManager
     * @throws InstantiationException no modem management system detected
     */
    public synchronized static ModemStatusManager getInstance(Context context, int instanceId)
    throws InstantiationException {
        if (ModemStatusManager.sInstances.containsKey(instanceId)) {
            return ModemStatusManager.sInstances.get(instanceId);
        } else {
            ModemStatusManager instance = new ModemStatusManager(context, instanceId);
            ModemStatusManager.sInstances.put(instanceId, instance);
            return instance;
        }
    }

    /**
     * Returns the instance of ModemStatusManager matching the instanceId
     * This function is kept for backward compatibility.
     *
     * @return The instance of ModemStatusManager
     * @throws InstantiationException if no modem management system detected
     */
    public static ModemStatusManager getInstance(Context context) throws InstantiationException {
        return getInstance(context, Constants.DEFAULT_INSTANCE);
    }

    /**
     * Requests a modem reset to the Modem Status Monitor service.
     * AP logs are automatically attached in the event report by calling this function
     *
     * @throws ModemClientException if the service returned an error or if a communication error
     *                              occurred between the client and the service.
     */
    public void resetModem() throws ModemClientException {
        if (this.modemStatusMonitor != null) {
            this.modemStatusMonitor.resetModem(null, DebugInfoLog.DBG_DEFAULT_LOG_SIZE,
                                               DebugInfoLog.DBG_DEFAULT_NO_LOG,
                                               DebugInfoLog.DBG_DEFAULT_NO_LOG);
        }
    }

    /**
     * Requests a modem reset to the Modem Status Monitor service.
     * AP logs are automatically attached in the event report by calling this function
     *
     * @param [in] causes Array of String describing the reason. Will be reported to crashtool
     *
     * @throws ModemClientException if the service returned an error or if a communication error
     *                              occurred between the client and the service.
     */
    public void resetModem(String[] causes) throws ModemClientException {
        if (this.modemStatusMonitor != null) {
            this.modemStatusMonitor.resetModem(causes, DebugInfoLog.DBG_DEFAULT_LOG_SIZE,
                                               DebugInfoLog.DBG_DEFAULT_NO_LOG,
                                               DebugInfoLog.DBG_DEFAULT_NO_LOG);
        }
    }

    /**
     * Requests a modem reset to the Modem Status Monitor service.
     *
     * @param [in] causes Array of String describing the reason. Will be reported to crashtool
     * @param [in] apLogsSize Size of ap logs to be attached in MBytes,
     *              -1: default, 0: logs not requested
     * @param [in] bpLogsSize Size of bp logs to be attached in MBytes,
     *              -1: default, 0: logs not requested
     * @param [in] bpLogsTime Time of the logs to attach in seconds,
     *              -1: default, 0: logs not requested
     *
     * @throws ModemClientException if the service returned an error or if a communication error
     *                              occurred between the client and the service.
     */
    public void resetModem(String[] causes, int apLogsSize, int bpLogsSize, int bpLogsTime)
    throws ModemClientException {
        if (this.modemStatusMonitor != null) {
            this.modemStatusMonitor.resetModem(causes, apLogsSize, bpLogsSize, bpLogsTime);
        }
    }

    /**
     * Requests a modem reset asynchronously (call is not blocking).
     * AP logs are automatically attached in the event report by calling this function
     *
     * @param listener The listener to get notified upon operation result.
     */
    public void resetModemAsync(final AsyncOperationResultListener listener) {
        if (this.modemStatusMonitor != null) {
            resetModemAsync(listener, null);
        }
    }

    /**
     * Requests a modem reset asynchronously (call is not blocking).
     * AP logs are automatically attached in the event report by calling this function
     *
     * @param listener The listener to get notified upon operation result.
     * @param causes   array of string that will be reported to crashlogd
     */
    public void resetModemAsync(final AsyncOperationResultListener listener, String[] causes) {
        new AsyncOperationTask(AsyncOperationTask.OPERATION_RESET_MODEM, listener, causes).execute();
    }

    /**
     * Requests a modem update to the Modem Status Monitor service.
     * No logs are attached in the event report by calling this function
     *
     * @throws ModemClientException if the service returned an error or if a communication error
     *                              occurred between the client and the service.
     */
    public void updateModem() throws ModemClientException {
        if (this.modemStatusMonitor != null) {
            this.modemStatusMonitor.updateModem();
        }
    }

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
     * @throws ModemClientException if the service returned an error or if a communication error
     *                              occurred between the client and the service.
     */
    public void notifyDebugInfo(String[] causes, int type, int apLogsSize, int bpLogsSize,
                                int bpLogsTime) throws ModemClientException {
        if (this.modemStatusMonitor != null) {
            this.modemStatusMonitor.notifyDebugInfo(causes, type,
                                                    apLogsSize, bpLogsSize, bpLogsTime);
        }
    }

    /**
     * Requests a modem recover asynchronously (call is not blocking).
     *
     * @param listener The listener to get notified upon operation result.
     */
    public void updateModemAsync(final AsyncOperationResultListener listener) {
        if (this.modemStatusMonitor != null) {
            updateModemAsync(listener, null);
        }
    }

    /**
     * Requests a modem recover asynchronously (call is not blocking).
     *
     * @param listener The listener to get notified upon operation result.
     * @param causes   array of string that will be reported to crashlogd
     */
    public void updateModemAsync(final AsyncOperationResultListener listener, String[] causes) {
        new AsyncOperationTask(AsyncOperationTask.OPERATION_UPDATE_MODEM, listener, causes).execute();
    }

    /**
     * Requests a modem shutdown to the Modem Status Monitor service.
     * This function forces a modem shutdown even if client(s) hold the resource
     *
     * @throws ModemClientException if the service returned an error or if a communication error
     *                              occurred between the client and the service.
     */
    public void shutdownModem() throws ModemClientException {
        if (this.modemStatusMonitor != null) {
            this.modemStatusMonitor.shutdownModem();
        }
    }

    /**
     * Requests a modem shutdown asynchronously (call is not blocking).
     * This function forces a modem shutdown even if client(s) hold the resource
     *
     * @param listener The listener to get notified upon operation result.
     */
    public void shutdownModemAsync(final AsyncOperationResultListener listener) {
        new AsyncOperationTask(AsyncOperationTask.OPERATION_SHUTDOWN_MODEM, listener).execute();
    }

    /**
     * Requests a modem lock to the Modem Status Monitor service.
     * Client must wait for onModemUp event before using it.
     *
     * @throws ModemClientException if the service returned an error or if a communication error
     *                              occurred between the client and the service.
     */
    public void acquireModem() throws ModemClientException {
        if (this.modemStatusMonitor != null) {
            this.modemStatusMonitor.acquireModem();
        }
    }

    /**
     * Requests a modem acquisition asynchronously (call is not blocking).
     * Client must wait for onModemUp event before using it.
     *
     * @param listener The listener to get notified upon operation result.
     */
    public void acquireModemAsync(final AsyncOperationResultListener listener) {
        new AsyncOperationTask(AsyncOperationTask.OPERATION_ACQUIRE_MODEM, listener).execute();
    }

    /**
     * Requests a modem release to the Modem Status Monitor service.
     * According to modem strategy, modem may be turned off on the last
     * resource release.
     *
     * @throws ModemClientException if the service returned an error or if a communication error
     *                              occurred between the client and the service.
     */
    public void releaseModem() throws ModemClientException {
        if (this.modemStatusMonitor != null) {
            this.modemStatusMonitor.releaseModem();
        }
    }

    /**
     * Requests a modem release asynchronously (call is not blocking).
     * According to modem strategy, modem may be turned off on the last
     * resource release.
     *
     * @param listener The listener to get notified upon operation result.
     */
    public void releaseModemAsync(final AsyncOperationResultListener listener) {
        new AsyncOperationTask(AsyncOperationTask.OPERATION_RELEASE_MODEM, listener).execute();
    }

    /**
     * Connects to the Modem Status Monitor service. You must call this method
     * to get your implementation of ModemEventListener called.
     *
     * @param clientName
     * @throws ModemClientException if the service returned an error or if a communication error
     *                              occurred between the client and the service.
     */
    public void connect(String clientName) throws ModemClientException {
        if (this.modemStatusMonitor != null) {
            this.modemStatusMonitor.connect(clientName, mInstanceId);
        }
    }

    /**
     * Requests a connection asynchronously (call is not blocking).
     *
     * @param clientName name of the client
     * @param listener   The listener to get notified upon operation result.
     */
    public void connectAsync(String clientName, final AsyncOperationResultListener listener) {
        new AsyncOperationTask(AsyncOperationTask.OPERATION_CONNECT, listener).execute(clientName);
    }

    /**
     * @param status  The modem status to wait for
     * @param timeout The maximum amount of time (in milliseconds) to wait
     * @return True if the status was received; otherwise False
     * @throws ModemClientException On any error
     */
    public boolean waitForModemStatus(ModemStatus status, long timeout) throws ModemClientException {
        if (this.modemStatusMonitor != null) {
            return this.modemStatusMonitor.waitForModemStatus(status, timeout);
        }
        return false;
    }

    /**
     * Disconnects from the Modem Status Monitor service. After calling this
     * method, the implementation of ModemEventListener will not be notified
     * anymore.
     */
    public void disconnect() {
        try {
            if (this.modemStatusMonitor != null) {
                this.modemStatusMonitor.disconnect();
            }
        } finally {
            this.eventListener = null;
        }
    }

    /**
     * Requests a disconnection asynchronously (call is not blocking).
     *
     * @param listener The listener to get notified upon operation result.
     */
    public void disconnectAsync(final AsyncOperationResultListener listener) {
        new AsyncOperationTask(AsyncOperationTask.OPERATION_DISCONNECT, listener).execute();
    }

    /**
     * Registers / subscribes an implementation of ModemEventListener to receive
     * modem events.
     *
     * @param listener The listener to register.
     * @param status   The bit mask of the modem statuses to listen to. // DEPRECATED. Remove this
     * @return The same instance of ModemStatusManager (this).
     * @throws ModemClientException if the service returned an error or if a communication error
     *                              occurred between the client and the service.
     */
    public synchronized ModemStatusManager subscribeToEvent(ModemEventListener listener, ModemStatus status)
    throws ModemClientException {
        this.eventListener = listener;
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.os.Handler.Callback#handleMessage(android.os.Message)
     */
    public boolean handleMessage(Message msg) {
        if ((msg != null) && (this.eventListener != null)) {
            int state = (int)msg.obj;
            // let's get the list of listeners interested by our event
            synchronized (this.eventListener) {
                switch (state) {
                case MDM_UP:
                    this.eventListener.onModemUp();
                    break;
                case MDM_DOWN:
                    this.eventListener.onModemDown();
                    break;
                case MDM_DEAD:
                    this.eventListener.onModemDead();
                    break;
                default:
                    break;
                }
            }
        }
        return true;
    }

    public static int getDefaultInstanceId() {
        return Constants.DEFAULT_INSTANCE;
    }

    private class AsyncOperationTask extends AsyncTask<Object, Void, Exception> {
        private AsyncOperationResultListener listener = null;
        private int requiredOperation = 0;
        private String[] causes = null;

        public static final int OPERATION_ACQUIRE_MODEM = 1;
        public static final int OPERATION_RELEASE_MODEM = 2;
        public static final int OPERATION_RESET_MODEM = 3;
        public static final int OPERATION_UPDATE_MODEM = 4;
        public static final int OPERATION_CONNECT = 5;
        public static final int OPERATION_DISCONNECT = 6;
        public static final int OPERATION_SHUTDOWN_MODEM = 7;

        public AsyncOperationTask(int requiredOperation, AsyncOperationResultListener listener) {
            this.listener = listener;
            this.requiredOperation = requiredOperation;
        }

        public AsyncOperationTask(int requiredOperation, AsyncOperationResultListener listener,
                                  String[] causes) {
            this.listener = listener;
            this.requiredOperation = requiredOperation;
            this.causes = causes;
        }

        @Override
        protected Exception doInBackground(Object ... params) {
            Exception ret = null;

            try {
                switch (this.requiredOperation) {
                case AsyncOperationTask.OPERATION_ACQUIRE_MODEM:
                    ModemStatusManager.this.acquireModem();
                    break;
                case AsyncOperationTask.OPERATION_RELEASE_MODEM:
                    ModemStatusManager.this.releaseModem();
                    break;
                case AsyncOperationTask.OPERATION_RESET_MODEM:
                    ModemStatusManager.this.resetModem();
                    break;
                case AsyncOperationTask.OPERATION_UPDATE_MODEM:
                    ModemStatusManager.this.updateModem();
                    break;
                case AsyncOperationTask.OPERATION_CONNECT:
                    if (params != null && params.length > 0) {
                        ModemStatusManager.this.connect((String)(params[0]));
                    }
                    break;
                case AsyncOperationTask.OPERATION_DISCONNECT:
                    ModemStatusManager.this.disconnect();
                    break;
                case AsyncOperationTask.OPERATION_SHUTDOWN_MODEM:
                    ModemStatusManager.this.shutdownModem();
                    break;
                }
            } catch (Exception ex) {
                ret = ex;
            }
            return ret;
        }

        @Override
        protected void onPostExecute(Exception result) {
            super.onPostExecute(result);
            if (listener != null) {
                if (result != null) {
                    listener.onOperationError(result);
                } else {
                    listener.onOperationComplete();
                }
            }
        }
    }
}

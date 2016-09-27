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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef __MDM_CLI_HEADER__
#define __MDM_CLI_HEADER__

#include "mdm_cli_dbg.h"

#ifdef __cplusplus
extern "C" {
#endif

#define MDM_CLI_NAME_LEN 64
#define MDM_CLI_DEFAULT_INSTANCE 1

typedef void mdm_cli_hdle_t;

/**
 * Events received by clients:
 * (NB: do not forget to update ModemStatusManager.java if those ones are updated)
 */
typedef enum mdm_cli_event {
    /* Modem is not available (can be ON electrically) */
    MDM_DOWN = 1,
    /* Modem is ON electrically */
    MDM_ON,
    /* Modem is running */
    MDM_UP,
    /* Modem is out of service */
    MDM_OOS,
    /* Those two events need to be acknowledge by the clients
     * if registered to those events */
    MDM_COLD_RESET,
    MDM_SHUTDOWN,
    /* Messages after MDM_DBG_INFO are pass through messages */
    MDM_DBG_INFO,
    /* Those events are here to ensure backward compatibility with
     * current MMGR implementation */
    MDM_TLV_SYNCING,
    MDM_NUM_EVENTS,
} mdm_cli_event_t;

/**
 * Structure used by the callback function
 *
 * @var id Event ID
 * @var context Client context. Used by clients to retrieve their own context
 * @var data_size Size of structure pointed by data
 * @var data Pointer to a structure. The pointer type is linked to the event ID
 */
typedef struct mdm_cli_callback_data {
    mdm_cli_event_t id;
    void *context;
    size_t data_size;
    void *data;
} mdm_cli_callback_data_t;

/**
 * Callback prototype.
 *
 * Note that if the callback(s) associated to the MDM_COLD_RESET and MDM_SHUTDOWN
 * event(s) do not return 0, the client will have to manually acknowledge them through
 * the mdm_cli_ack_cold_reset / mdm_cli_ack_shutdown functions.
 */
typedef int (*mdm_cli_callback_t) (const mdm_cli_callback_data_t *);

/**
 * Structure used to register a callback to an event
 *
 * @var id Event ID
 * @var callback Associated callback
 * @var context Pointer passed back to the client when the callback for this
 * event is called. Not accessed/interpreted by server, pass-through only.
 */
typedef struct mdm_cli_register {
    mdm_cli_event_t id;
    mdm_cli_callback_t callback;
    void *context;
} mdm_cli_register_t;

/**
 * Enum describing the restart reason
 */
typedef enum mdm_cli_restart_cause {
    RESTART_MDM_OOS = 1,  /* DEPRECATED. Must be used only by NVM server */
    RESTART_MDM_ERR,
    RESTART_APPLY_UPDATE,
} mdm_cli_restart_cause_t;

/**
 * Initializes the Modem Manager client library and connects to Modem Manager system.
 * Client provides its name and a structure with events to monitor with
 * associated callbacks
 *
 * @param [in] client_name Name of the client. Must be NULL terminated. Must
 * not exceed MDM_CLI_NAME_LEN length
 * @param [in] inst_id Modem Management instance ID
 * (for single modem platform, client must provide MDM_CLI_DEFAULT_INSTANCE
 * value)
 * @param [in] nb_evts Number of events to register
 * @param [in] evts Array with nb_evts entries containing the registered events
 * (id and its associated callback). The same callback can be used for all
 * events.
 *
 * @return a valid handle. Must be freed by calling the disconnect function
 * @return NULL otherwise
 */
mdm_cli_hdle_t *mdm_cli_connect(const char *client_name, int inst_id, int nb_evts,
                                const mdm_cli_register_t evts[]);

/**
 * Disconnects the client to Modem Management system and disposes the handle.
 * Modem resource for this client is implicitly released (if not already done)
 * by calling this function.
 *
 * @param [in] hdle
 *
 * @return 0 if successful
 * @return -1 otherwise
 */
int mdm_cli_disconnect(mdm_cli_hdle_t *hdle);

/**
 * Acquires the modem resource. Client must way for MDM_UP event before using
 * it.
 *
 * @param [in] hdle
 *
 * @return 0 if successful
 * @return -1 otherwise
 */
int mdm_cli_acquire(mdm_cli_hdle_t *hdle);

/**
 * Releases the modem resource. According to modem strategy, modem may be turned
 * off on the last resource release.
 *
 * @param [in] hdle
 *
 * @return 0 if successful
 * @return -1 otherwise
 */
int mdm_cli_release(mdm_cli_hdle_t *hdle);

/**
 * Restarts the modem.
 *
 * @param [in] hdle
 * @param [in] cause
 * @param [in] data Data is an array of string used by the client to describe
 * the reason of the modem restart. This data is forwarded to all clients subscribed
 * to the MDM_DBG_INFO event. data can be NULL and if not, data[0] must be the
 * client name.
 *
 * @return 0 if successful
 * @return -1 otherwise
 */
int mdm_cli_restart(mdm_cli_hdle_t *hdle, mdm_cli_restart_cause_t cause,
                    const mdm_cli_dbg_info_t *data);

/**
 * Shutdowns the modem. This function forces a modem shutdown even if client(s)
 * hold the resource.
 *
 * @param [in] hdle
 *
 * @return 0 if successful
 * @return -1 otherwise
 */
int mdm_cli_shutdown(mdm_cli_hdle_t *hdle);

/**
 * Backs up modem NVM data. Modem NVM data and calibration are flushed on AP
 * file system.
 *
 * @param [in] hdle
 *
 * @return 0 if successful
 * @return -1 otherwise
 */
int mdm_cli_nvm_bckup(mdm_cli_hdle_t *hdle);

/**
 * Acknowledges that the modem cold reset preparation is finished.
 * Used by Modem Management to ensure that all clients subscribed to cold
 * reset event have finished their event processing before resetting the modem.
 * This is done so as to prevent electrical leakage.
 *
 * @param [in] hdle
 *
 * @return 0 if successful
 * @return -1 otherwise
 */
int mdm_cli_ack_cold_reset(mdm_cli_hdle_t *hdle);

/**
 * Acknowledges that the modem shutdown preparation is finished.
 * Used by Modem Management to ensure that all clients subscribed to modem
 * shutdown event have finished their event processing before resetting the
 * modem. This is done so as to prevent electrical leakage.
 *
 * @param [in] hdle
 *
 * @return 0 if successful
 * @return -1 otherwise
 */
int mdm_cli_ack_shutdown(mdm_cli_hdle_t *hdle);

/**
 * Broadcasts to all subscribed clients the given debug info structure.
 *
 * @param [in] hdle
 * @param [in] data Data is an array of string used by the client to report
 * a debugging event. This data is forwarded to all clients subscribed to the
 * MDM_DBG_INFO event.
 *
 * @return 0 if successful
 * @return -1 otherwise
 */
int mdm_cli_notify_dbg(mdm_cli_hdle_t *hdle, const mdm_cli_dbg_info_t *data);

#ifdef __cplusplus
}
#endif

#endif /* __MDM_CLI_HEADER__ */

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
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef __MDM_CLI_DBG_HEADER__
#define __MDM_CLI_DBG_HEADER__

/**
 * This header defines all structures used by the modem management client
 * library for debug purpose
 */

#include <sys/types.h>

#define MDM_CLI_MAX_NB_DATA 6
#define MDM_CLI_MAX_LEN_DATA 512

/* Those strings are used by MMGR to provide the core dump retrieval
 * verdict to crashlog. */
#define DUMP_STR_SUCCEED "CD_SUCCEED"
#define DUMP_STR_TIMEOUT "CD_TIMEOUT"
#define DUMP_STR_LINK_ERR "CD_LINK_ERROR"
#define DUMP_STR_SELF_RESET "CD_SELF_RESET"
#define DUMP_STR_PROTOCOL "CD_PROTOCOL_ERROR"
#define DUMP_STR_OTHER "OTHER"

#define DBG_DEFAULT_LOG_SIZE -1
#define DBG_DEFAULT_NO_LOG 0
#define DBG_DEFAULT_LOG_TIME -1

#define MDM_CLI_INIT_DBG_INFO(dbg_info) do { \
        (mdm_cli_dbg_info_t){dbg_info.type = DBG_TYPE_INFO, \
                             dbg_info.ap_logs_size = DBG_DEFAULT_NO_LOG, \
                             dbg_info.bp_logs_size = DBG_DEFAULT_NO_LOG, \
                             dbg_info.bp_logs_time = DBG_DEFAULT_NO_LOG, dbg_info.nb_data = 0, \
                             dbg_info.data = NULL }; \
} while (0)

typedef enum mdm_cli_dbg_type {
    DBG_TYPE_STATS = 1,
    DBG_TYPE_INFO,
    DBG_TYPE_ERROR,
    DBG_TYPE_PLATFORM_REBOOT,
    DBG_TYPE_DUMP_START,
    DBG_TYPE_DUMP_END,
    DBG_TYPE_DUMP_SKIP,
    DBG_TYPE_APIMR,
    DBG_TYPE_SELF_RESET,
    DBG_TYPE_FW_SUCCES,
    DBG_TYPE_FW_FAILURE,
    DBG_TYPE_TLV_NONE,
    DBG_TYPE_TLV_SUCCESS,
    DBG_TYPE_TLV_FAILURE,
    DBG_TYPE_NVM_BACKUP_SUCCESS,
    DBG_TYPE_NVM_BACKUP_FAILURE,
    DBG_TYPE_CAL_UPDATE_SUCCESS,
    DBG_TYPE_CAL_UPDATE_FAILURE,
    DBG_TYPE_NUM,
} mdm_cli_dbg_type_t;

/**
 * Structure used to raise debug information to crashtool
 *
 * @var type Type of debug information
 * @var ap_logs_size Size of AP logs to be attached in MBytes
 * @var bp_logs_size Size of BP logs to be attached in MBytes
 * @var bp_logs_time BP logs depth to be captured in seconds
 * @var nb_data Number of entries in data array
 * @var data array of (up to MDM_CLI_MAX_NB_DATA) string pointers
 * (each of maximum length MDM_CLI_MAX_LEN_DATA bytes, including 0 terminator)
 */
typedef struct mdm_cli_dbg_info {
    mdm_cli_dbg_type_t type;
    int ap_logs_size;
    int bp_logs_size;
    int bp_logs_time;
    size_t nb_data;
    const char **data;
} mdm_cli_dbg_info_t;

#endif /* __MDM_CLI_DBG_HEADER__ */

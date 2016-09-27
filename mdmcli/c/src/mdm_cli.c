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

#include <stdlib.h>
#include <dlfcn.h>

#include "mdm_cli.h"
#include "tcs.h"

#define LOG_NDEBUG 0
#define LOG_TAG "MDM_CLI"
#include <utils/Log.h>

typedef struct mdm_cli_api {
    mdm_cli_hdle_t * (*connect)(const char *client_name, int inst_id,
                                int nb_evts, const mdm_cli_register_t evts[]);
    int (*disconnect)(mdm_cli_hdle_t *hdle);
    int (*acquire)(mdm_cli_hdle_t *hdle);
    int (*release)(mdm_cli_hdle_t *hdle);
    int (*restart)(mdm_cli_hdle_t *hdle, mdm_cli_restart_cause_t cause,
                   const mdm_cli_dbg_info_t *data);
    int (*shtdwn)(mdm_cli_hdle_t *hdle);
    int (*nvm_bkup)(mdm_cli_hdle_t *hdle);
    int (*ack_cold)(mdm_cli_hdle_t *hdle);
    int (*ack_shtdwn)(mdm_cli_hdle_t *hdle);
    int (*notify_dbg)(mdm_cli_hdle_t *hdle, const mdm_cli_dbg_info_t *data);
} mdm_cli_api_t;

typedef struct mdm_cli_ctx {
    void *lib;
    mdm_cli_api_t api;
    mdm_cli_hdle_t *hdle;
} mdm_cli_ctx_t;

/**
 * Retrieves the name of the library to load
 *
 * @param [in] cfg_id Modem instance ID
 *
 * @return the library name. Must be freed by caller
 * @return NULL if the library is not found
 */
static char *mdm_cli_get_lib_name(size_t cfg_id)
{
    char *lib = NULL;
    tcs_handle_t *tcs = tcs_init();

    if (tcs) {
        tcs_cfg_t *cfg = tcs_get_config(tcs);

        if (cfg && (cfg_id <= cfg->nb)) {
            tcs_libs_t *libs = NULL;

            if ((cfg->mdm_struct_type == TCS_CFG_TYPE_XMM) &&
                (cfg->mdm_struct_size == sizeof(tcs_mdm_info_XMM_t)) &&
                cfg->mdm_XMM)
                libs = &cfg->mdm_XMM[cfg_id].libs;
            else if ((cfg->mdm_struct_type == TCS_CFG_TYPE_SOFIA) &&
                     (cfg->mdm_struct_size == sizeof(tcs_mdm_info_SOFIA_t)) &&
                     cfg->mdm_SOFIA)
                libs = &cfg->mdm_SOFIA[cfg_id].libs;

            if (libs && libs->lib) {
                for (ssize_t i = (ssize_t)libs->nb - 1; i >= 0;
                     i--) {
                    if (!strcmp(libs->lib[i].name, "mdmcli")) {
                        lib = strdup(libs->lib[i].filename);
                        break;
                    }
                }
            }
        }
        tcs_dispose(tcs);
    }

    if (!lib) {
        ALOGE("Library name not found in TCS, use stub");
        lib = strdup("libmdmcli_stub.so");
    } else {
        ALOGD("library name: %s", lib);
    }

    return lib;
}

/**
 * Loads client implementation
 *
 * @param [in] ctx Module context
 * @param [in] filename Library name
 *
 * @return 0 if successful
 * @return -1 otherwise
 */
static int mdm_get_load_lib(mdm_cli_ctx_t *ctx, const char *filename)
{
    int ret = -1;

    if (ctx && filename) {
        dlerror(); // Clear previous errors if any
        ctx->lib = dlopen(filename, RTLD_LAZY);
        if (ctx->lib) {
            ctx->api.connect = dlsym(ctx->lib, "mdm_cli_connect");
            ctx->api.disconnect = dlsym(ctx->lib, "mdm_cli_disconnect");
            ctx->api.acquire = dlsym(ctx->lib, "mdm_cli_acquire");
            ctx->api.release = dlsym(ctx->lib, "mdm_cli_release");
            ctx->api.restart = dlsym(ctx->lib, "mdm_cli_restart");
            ctx->api.shtdwn = dlsym(ctx->lib, "mdm_cli_shutdown");
            ctx->api.nvm_bkup = dlsym(ctx->lib, "mdm_cli_nvm_bckup");
            ctx->api.ack_cold = dlsym(ctx->lib, "mdm_cli_ack_cold_reset");
            ctx->api.ack_shtdwn = dlsym(ctx->lib, "mdm_cli_ack_shutdown");
            ctx->api.notify_dbg = dlsym(ctx->lib, "mdm_cli_notify_dbg");
        }
        const char *err = dlerror();
        if (err)
            ALOGE("%s", err);
        else
            ret = 0;
    }

    return ret;
}

/**
 * @see mdm_cli.h
 */
mdm_cli_hdle_t *mdm_cli_connect(const char *client_name, int inst_id,
                                int nb_evts, const mdm_cli_register_t evts[])
{
    mdm_cli_ctx_t *ctx = NULL;

    if (client_name && (inst_id > 0)) {
        char *filename = mdm_cli_get_lib_name(inst_id - 1);
        ctx = calloc(1, sizeof(mdm_cli_ctx_t));

        if (ctx && !mdm_get_load_lib(ctx, filename)) {
            ctx->hdle = ctx->api.connect(client_name, inst_id, nb_evts, evts);
            if (!ctx->hdle) {
                mdm_cli_disconnect((mdm_cli_hdle_t *)ctx);
                ctx = NULL;
            }
        }

        free(filename);
    }

    return (mdm_cli_hdle_t *)ctx;
}

/**
 * @see mdm_cli.h
 */
int mdm_cli_disconnect(mdm_cli_hdle_t *hdle)
{
    int ret = -1;
    mdm_cli_ctx_t *ctx = (mdm_cli_ctx_t *)hdle;

    if (ctx) {
        if ((ctx->hdle) && (ctx->api.disconnect))
            ret = ctx->api.disconnect(ctx->hdle);
        if (ctx->lib)
            dlclose(ctx->lib);
        free(ctx);
    }

    return ret;
}

/**
 * @see mdm_cli.h
 */
int mdm_cli_acquire(mdm_cli_hdle_t *hdle)
{
    int ret = -1;
    mdm_cli_ctx_t *ctx = (mdm_cli_ctx_t *)hdle;

    if (ctx && ctx->hdle && ctx->api.acquire)
        ret = ctx->api.acquire(ctx->hdle);

    return ret;
}

/**
 * @see mdm_cli.h
 */
int mdm_cli_release(mdm_cli_hdle_t *hdle)
{
    int ret = -1;
    mdm_cli_ctx_t *ctx = (mdm_cli_ctx_t *)hdle;

    if (ctx && ctx->hdle && ctx->api.release)
        ret = ctx->api.release(ctx->hdle);

    return ret;
}

/**
 * @see mdm_cli.h
 */
int mdm_cli_restart(mdm_cli_hdle_t *hdle, mdm_cli_restart_cause_t cause,
                    const mdm_cli_dbg_info_t *data)
{
    int ret = -1;
    mdm_cli_ctx_t *ctx = (mdm_cli_ctx_t *)hdle;
    const mdm_cli_dbg_info_t *dbg_info = (cause == RESTART_APPLY_UPDATE) ? NULL : data;

    if (ctx && ctx->hdle && ctx->api.restart)
        ret = ctx->api.restart(ctx->hdle, cause, dbg_info);

    return ret;
}

/**
 * @see mdm_cli.h
 */
int mdm_cli_shutdown(mdm_cli_hdle_t *hdle)
{
    int ret = -1;
    mdm_cli_ctx_t *ctx = (mdm_cli_ctx_t *)hdle;

    if (ctx && ctx->hdle && ctx->api.shtdwn)
        ret = ctx->api.shtdwn(ctx->hdle);

    return ret;
}

/**
 * @see mdm_cli.h
 */
int mdm_cli_nvm_bckup(mdm_cli_hdle_t *hdle)
{
    int ret = -1;
    mdm_cli_ctx_t *ctx = (mdm_cli_ctx_t *)hdle;

    if (ctx && ctx->hdle && ctx->api.nvm_bkup)
        ret = ctx->api.nvm_bkup(ctx->hdle);

    return ret;
}

/**
 * @see mdm_cli.h
 */
int mdm_cli_ack_cold_reset(mdm_cli_hdle_t *hdle)
{
    int ret = -1;
    mdm_cli_ctx_t *ctx = (mdm_cli_ctx_t *)hdle;

    if (ctx && ctx->hdle && ctx->api.ack_cold)
        ret = ctx->api.ack_cold(ctx->hdle);

    return ret;
}

/**
 * @see mdm_cli.h
 */
int mdm_cli_ack_shutdown(mdm_cli_hdle_t *hdle)
{
    int ret = -1;
    mdm_cli_ctx_t *ctx = (mdm_cli_ctx_t *)hdle;

    if (ctx && ctx->hdle && ctx->api.ack_shtdwn)
        ret = ctx->api.ack_shtdwn(ctx->hdle);

    return ret;
}

/**
 * @see mdm_cli.h
 */
int mdm_cli_notify_dbg(mdm_cli_hdle_t *hdle, const mdm_cli_dbg_info_t *data)
{
    int ret = -1;
    mdm_cli_ctx_t *ctx = (mdm_cli_ctx_t *)hdle;

    if (ctx && ctx->hdle && ctx->api.notify_dbg)
        ret = ctx->api.notify_dbg(ctx->hdle, data);

    return ret;
}

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

#include "mdm_cli.h"

#if defined(STDIO_LOGS)
#define ALOGE printf
#define ALOGD printf
#else
#define LOG_TAG "MDMCLISTUB"
#include <utils/Log.h>
#endif

#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#define xstr(s) str(s)
#define str(s) #s

#define ASSERT(exp) do { \
        if (!(exp)) { \
            ALOGE("%s:%d Assertion '" xstr(exp) "'\n", __FILE__, __LINE__); \
            abort(); \
        } \
} while (0)

typedef struct mdm_cli_ctx {
    mdm_cli_register_t evts[MDM_NUM_EVENTS];
    int nb_events;
    char *client_name;
} mdm_cli_ctx_t;

static void *notify_oos(void *data)
{
    mdm_cli_ctx_t *ctx = (mdm_cli_ctx_t *)data;
    mdm_cli_callback_data_t cb_data = { MDM_OOS, ctx->evts[MDM_OOS].context, 0, NULL };

    ASSERT(ctx->evts[MDM_OOS].callback != NULL);

    ALOGD("notifying client '%s' that modem is OOS", ctx->client_name);
    ctx->evts[MDM_OOS].callback(&cb_data);

    return NULL;
}

/**
 * @see mdm_cli.h
 */
mdm_cli_hdle_t *mdm_cli_connect(const char *client_name, int inst_id,
                                int nb_evts, const mdm_cli_register_t evts[])
{
    (void)inst_id;

    ASSERT(nb_evts <= MDM_NUM_EVENTS);
    ASSERT(client_name != NULL);

    mdm_cli_ctx_t *ctx = calloc(1, sizeof(mdm_cli_ctx_t));
    ASSERT(ctx != NULL);

    ctx->client_name = strdup(client_name);
    ASSERT(ctx->client_name != NULL);

    unsigned int evt_bitmap = 0;
    for (int i = 0; i < nb_evts; i++) {
        ASSERT(evts[i].id < MDM_NUM_EVENTS);
        evt_bitmap |= 1u << evts[i].id;
        ctx->evts[evts[i].id].id = evts[i].id;
        ctx->evts[evts[i].id].callback = evts[i].callback;
        ctx->evts[evts[i].id].context = evts[i].context;
    }
    ALOGD("client '%s' connected (0x%xu)", client_name, evt_bitmap);

    /* Modem is OOS by default so call client callback (if registered).
     * The callback needs to be called in a different thread, otherwise, the JNI will crash */
    if (ctx->evts[MDM_OOS].callback) {
        pthread_t thid;
        pthread_attr_t attr;
        ASSERT(pthread_attr_init(&attr) == 0);
        ASSERT(pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED) == 0);
        ASSERT(pthread_create(&thid, &attr, notify_oos, ctx) == 0);
        ASSERT(pthread_attr_destroy(&attr) == 0);
    }

    return (mdm_cli_hdle_t *)ctx;
}

/**
 * @see mdm_cli.h
 */
int mdm_cli_disconnect(mdm_cli_hdle_t *hdle)
{
    ASSERT(hdle != NULL);
    mdm_cli_ctx_t *ctx = (mdm_cli_ctx_t *)hdle;
    ALOGD("%s: client '%s'", __FUNCTION__, ctx->client_name);
    free(ctx->client_name);
    free(ctx);

    return 0;
}

/**
 * @see mdm_cli.h
 */
int mdm_cli_acquire(mdm_cli_hdle_t *hdle)
{
    ASSERT(hdle != NULL);
    mdm_cli_ctx_t *ctx = (mdm_cli_ctx_t *)hdle;
    ALOGD("%s: client '%s'", __FUNCTION__, ctx->client_name);

    return 0;
}

/**
 * @see mdm_cli.h
 */
int mdm_cli_release(mdm_cli_hdle_t *hdle)
{
    ASSERT(hdle != NULL);
    mdm_cli_ctx_t *ctx = (mdm_cli_ctx_t *)hdle;
    ALOGD("%s: client '%s'", __FUNCTION__, ctx->client_name);

    return 0;
}

/**
 * @see mdm_cli.h
 */
int mdm_cli_restart(mdm_cli_hdle_t *hdle, mdm_cli_restart_cause_t cause,
                    const mdm_cli_dbg_info_t *data)
{
    (void)cause;
    (void)data;

    ASSERT(hdle != NULL);
    mdm_cli_ctx_t *ctx = (mdm_cli_ctx_t *)hdle;
    ALOGD("%s: client '%s'", __FUNCTION__, ctx->client_name);

    return 0;
}

/**
 * @see mdm_cli.h
 */
int mdm_cli_shutdown(mdm_cli_hdle_t *hdle)
{
    ASSERT(hdle != NULL);
    mdm_cli_ctx_t *ctx = (mdm_cli_ctx_t *)hdle;
    ALOGD("%s: client '%s'", __FUNCTION__, ctx->client_name);

    return 0;
}

/**
 * @see mdm_cli.h
 */
int mdm_cli_nvm_bckup(mdm_cli_hdle_t *hdle)
{
    ASSERT(hdle != NULL);
    mdm_cli_ctx_t *ctx = (mdm_cli_ctx_t *)hdle;
    ALOGD("%s: client '%s'", __FUNCTION__, ctx->client_name);

    return 0;
}

/**
 * @see mdm_cli.h
 */
int mdm_cli_ack_cold_reset(mdm_cli_hdle_t *hdle)
{
    ASSERT(hdle != NULL);
    mdm_cli_ctx_t *ctx = (mdm_cli_ctx_t *)hdle;
    ALOGD("%s: client '%s'", __FUNCTION__, ctx->client_name);

    return 0;
}

/**
 * @see mdm_cli.h
 */
int mdm_cli_ack_shutdown(mdm_cli_hdle_t *hdle)
{
    ASSERT(hdle != NULL);
    mdm_cli_ctx_t *ctx = (mdm_cli_ctx_t *)hdle;
    ALOGD("%s: client '%s'", __FUNCTION__, ctx->client_name);

    return 0;
}

/**
 * @see mdm_cli.h
 */
int mdm_cli_notify_dbg(mdm_cli_hdle_t *hdle, const mdm_cli_dbg_info_t *data)
{
    (void)data;

    ASSERT(hdle != NULL);
    mdm_cli_ctx_t *ctx = (mdm_cli_ctx_t *)hdle;
    ALOGD("%s: client '%s'", __FUNCTION__, ctx->client_name);

    return 0;
}

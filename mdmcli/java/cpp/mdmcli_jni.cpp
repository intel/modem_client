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


#define LOG_TAG "MDMCLI_JNI"

#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <assert.h>

#include "utils/Log.h"
#include "jni.h"

#include "mdm_cli.h"

#define LOGE(format, args ...) \
    do { ALOGE("%s - " format, __FUNCTION__, ## args); } while (0)
#define LOGD(format, args ...) \
    do { ALOGD("%s - " format, __FUNCTION__, ## args); } while (0)

#define ARRAY_SIZE(a) (sizeof(a) / sizeof(*a))

static struct
{
    jclass classRef;
    jmethodID callbackRef;
    JavaVM *mVm;
    mdm_cli_hdle_t *mMdmHdle;
} gContext;

static void callback(mdm_cli_event_t ev)
{
    JNIEnv *env;

    if (gContext.mVm->AttachCurrentThread(&env, NULL) == JNI_OK) {
        env->CallStaticVoidMethod(gContext.classRef, gContext.callbackRef, ev);
        gContext.mVm->DetachCurrentThread();
    }
}

static int modemEventCB(const mdm_cli_callback_data_t *event)
{
    if (!event) {
        return 0;
    }

    switch (event->id) {
    case MDM_DOWN:
        LOGD("Modem down");
        callback(MDM_DOWN);
        break;
    case MDM_UP:
        LOGD("Modem up");
        callback(MDM_UP);
        break;
    case MDM_OOS:
        LOGD("Modem out of service");
        callback(MDM_OOS);
        break;
    default:
        LOGE("Unknown event");
        break;
    }

    return 0;
}

static jint jni_acquireModem(JNIEnv *env, jclass clazz)
{
    (void)env;
    (void)clazz;

    return (jint)mdm_cli_acquire(gContext.mMdmHdle);
}

static jint jni_releaseModem(JNIEnv *env, jclass clazz)
{
    (void)env;
    (void)clazz;

    return (jint)mdm_cli_release(gContext.mMdmHdle);
}

static char **stringArrayToCharArray(JNIEnv *env, jobjectArray stringArray, size_t *size)
{
    char **charArray = NULL;

    if (env && stringArray) {
        *size = env->GetArrayLength(stringArray);
        *size = (*size > MDM_CLI_MAX_NB_DATA) ? MDM_CLI_MAX_NB_DATA : *size;

        charArray = (char **)malloc(sizeof(char *) * *size);

        if (charArray) {
            for (size_t i = 0; i < *size; i++) {
                jstring string = (jstring)env->GetObjectArrayElement(stringArray, i);
                const char *charString = env->GetStringUTFChars(string, 0);

                charArray[i] = strdup(charString);

                env->ReleaseStringUTFChars(string, charString);
                env->DeleteLocalRef(string);
            }
        }
    } else {
        *size = 0;
    }

    return charArray;
}

static int resetModem(JNIEnv *env, jobjectArray stringArray, mdm_cli_restart_cause_t cause,
                      int apLogsSize, int bpLogsSize, int bpLogsTime)
{
    size_t size = 0;
    char **charArray = stringArrayToCharArray(env, stringArray, &size);

    const mdm_cli_dbg_info_t dbg_info = {
        DBG_TYPE_APIMR, apLogsSize, bpLogsSize, bpLogsTime,
        size, (const char **)charArray
    };

    int ret = mdm_cli_restart(gContext.mMdmHdle, cause, &dbg_info);

    if (charArray) {
        for (size_t i = 0; i < size; i++) {
            free(charArray[i]);
        }
        free(charArray);
    }

    return ret;
}

static int dbgInfo(JNIEnv *env, jobjectArray stringArray, int type, int apLogsSize,
                   int bpLogsSize, int bpLogsTime)
{
    size_t size = 0;
    char **charArray = stringArrayToCharArray(env, stringArray, &size);

    const mdm_cli_dbg_info_t dbg_info = {
        (mdm_cli_dbg_type_t)type, apLogsSize, bpLogsSize, bpLogsTime,
        size, (const char **)charArray
    };

    int ret = mdm_cli_notify_dbg(gContext.mMdmHdle, &dbg_info);

    if (charArray) {
        for (size_t i = 0; i < size; i++) {
            free(charArray[i]);
        }
        free(charArray);
    }

    return ret;
}

static jint jni_resetModem(JNIEnv *env, jclass clazz, jobjectArray stringArray,
                           jint apLogsSize, jint bpLogsSize, jint bpLogsTime)
{
    (void)clazz;
    return (jint)resetModem(env, stringArray, RESTART_MDM_ERR, apLogsSize, bpLogsSize, bpLogsTime);
}

static jint jni_updateModem(JNIEnv *env, jclass clazz)
{
    (void)clazz;
    return (jint)resetModem(env, NULL, RESTART_APPLY_UPDATE, DBG_DEFAULT_NO_LOG,
                            DBG_DEFAULT_NO_LOG, DBG_DEFAULT_NO_LOG);
}

static jint jni_notifyDebugInfo(JNIEnv *env, jclass clazz, jobjectArray stringArray, jint type,
                                jint apLogsSize, jint bpLogsSize, jint bpLogsTime)
{
    (void)clazz;
    return (jint)dbgInfo(env, stringArray, type, apLogsSize, bpLogsSize, bpLogsTime);
}

static jint jni_shutdownModem(JNIEnv *env, jclass clazz)
{
    (void)env;
    (void)clazz;

    return (jint)mdm_cli_shutdown(gContext.mMdmHdle);
}

static void jni_connect(JNIEnv *env, jclass clazz, jstring clientName, jint inst_id)
{
    (void)clazz;
    const char *name = env->GetStringUTFChars(clientName, NULL);

    mdm_cli_register_t evts[] = {
        { MDM_DOWN, modemEventCB, NULL },
        { MDM_UP, modemEventCB, NULL },
        { MDM_OOS, modemEventCB, NULL },
    };

    while (!(gContext.mMdmHdle = mdm_cli_connect(name, inst_id, ARRAY_SIZE(evts), evts))) {
        LOGE("Failed to connect to modem management");
        sleep(1);
    }

    env->ReleaseStringUTFChars(clientName, name);
    LOGD("Connected to modem management");
}

static void jni_disconnect(JNIEnv *env, jclass clazz)
{
    (void)env;
    (void)clazz;

    mdm_cli_disconnect(gContext.mMdmHdle);
    gContext.mMdmHdle = NULL;
}

static int registerMethods(JNIEnv *env, jclass clazz)
{
    static const JNINativeMethod methods[] = {
        { "jni_connect", "(Ljava/lang/String;I)V", (void *)jni_connect },
        { "jni_disconnect", "()V", (void *)jni_disconnect },
        { "jni_resetModem", "([Ljava/lang/String;III)I", (void *)jni_resetModem },
        { "jni_updateModem", "()I", (void *)jni_updateModem },
        { "jni_notifyDebugInfo", "([Ljava/lang/String;IIII)I", (void *)jni_notifyDebugInfo },
        { "jni_acquireModem", "()I", (void *)jni_acquireModem },
        { "jni_releaseModem", "()I", (void *)jni_releaseModem },
        { "jni_shutdownModem", "()I", (void *)jni_shutdownModem },
    };

    return env->RegisterNatives(clazz, methods, ARRAY_SIZE(methods)) != JNI_OK;
}

jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JNIEnv *env = NULL;
    jint result = -1;
    jclass clazz;

    (void)reserved;

    if (vm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK) {
        LOGE("ERROR: GetEnv failed");
        return -1;
    }
    assert(env != NULL);

    gContext.mVm = vm;

    /* look up the class */
    static const char *const className = "com/intel/internal/telephony/JniModemStatusMonitor";
    clazz = env->FindClass(className);
    if (clazz == NULL) {
        LOGE("Can't find class %s", className);
        return -1;
    }

    gContext.classRef = (jclass)env->NewGlobalRef(clazz);
    if (gContext.classRef == NULL) {
        LOGE("cannot create new Global reference");
        return -1;
    }

    gContext.callbackRef = env->GetStaticMethodID(gContext.classRef, "callback", "(I)V");
    if (gContext.callbackRef == NULL) {
        LOGE("Failed to get callback reference");
        return -1;
    }

    if (registerMethods(env, clazz)) {
        LOGE("Failed to register methods");
        return -1;
    }

    LOGD("JNI loaded successfully");

    return JNI_VERSION_1_6;
}

void JNI_OnUnload(JavaVM *vm, void *reserved)
{
    (void)reserved;

    JNIEnv *env = NULL;
    if (vm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK) {
        LOGE("ERROR: GetEnv failed");
        return;
    }

    if (gContext.mMdmHdle) {
        mdm_cli_disconnect(gContext.mMdmHdle);
        gContext.mMdmHdle = NULL;
    }

    env->DeleteGlobalRef(gContext.classRef);
}

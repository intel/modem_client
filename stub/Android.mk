LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libmdmcli_stub
LOCAL_MODULE_TAGS := optional
LOCAL_PROPRIETARY_MODULE := true

LOCAL_C_INCLUDES := $(LOCAL_PATH)/../mdmcli/c/inc/

LOCAL_SRC_FILES  += mdm_cli_stub.c
LOCAL_CFLAGS := -Wall -Wvla -Wextra -Werror -std=gnu99 -Wunused-function

LOCAL_SHARED_LIBRARIES := libc libcutils liblog

include $(BUILD_SHARED_LIBRARY)

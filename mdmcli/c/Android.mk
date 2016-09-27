LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libmdmcli
LOCAL_MODULE_TAGS := optional
LOCAL_PROPRIETARY_MODULE := true

LOCAL_C_INCLUDES := $(LOCAL_PATH)/inc
LOCAL_CFLAGS := -Wall -Wvla -Wextra -Werror -std=c99 -Wunused-function
LOCAL_SRC_FILES := $(call all-c-files-under, src)
LOCAL_SHARED_LIBRARIES := libdl libcutils liblog libtcs
LOCAL_REQUIRED_MODULES := libmdmcli_stub

# The headers are exported that way for apps/libs which need them but are not
# linked to
LOCAL_COPY_HEADERS_TO := telephony/libmdmcli
LOCAL_COPY_HEADERS := $(call all-h-files-under, inc)

# For others, let's use a smarter way:
LOCAL_EXPORT_C_INCLUDE_DIRS := $(LOCAL_PATH)/inc

include $(BUILD_SHARED_LIBRARY)

LOCAL_PATH := $(call my-dir)

####################################
# ModemClient: interface
####################################
include $(CLEAR_VARS)

LOCAL_MODULE := com.intel.internal.telephony.ModemClient
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-java-files-under, java)
LOCAL_PROPRIETARY_MODULE := true

# Android build chain is not able to handle LOCAL_REQUIRED_MODULES for java STATIC libs
# JNI dependency must be declared in PRODUCT_PACKAGE or by the Modem Management

include $(BUILD_STATIC_JAVA_LIBRARY)

####################################
# libmdmcli_jni: JNI cpp
####################################
include $(CLEAR_VARS)

LOCAL_MODULE := libmdmcli_jni
LOCAL_MODULE_TAGS := optional
LOCAL_PROPRIETARY_MODULE := true

LOCAL_C_INCLUDES := $(JNI_H_INCLUDE)
LOCAL_SRC_FILES := cpp/mdmcli_jni.cpp

LOCAL_SHARED_LIBRARIES := libnativehelper liblog libmdmcli

LOCAL_PRELINK_MODULE := false

include $(BUILD_SHARED_LIBRARY)

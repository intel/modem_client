#############################################
# MODEM MANAGER java test application
#############################################
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_PACKAGE_NAME := ModemClientJavaTest
LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_STATIC_JAVA_LIBRARIES := \
    android-support-v4 \
    com.intel.internal.telephony.ModemClient

LOCAL_CERTIFICATE := platform
LOCAL_PROGUARD_ENABLED := disabled
LOCAL_DEX_PREOPT := nostripping

include $(BUILD_PACKAGE)

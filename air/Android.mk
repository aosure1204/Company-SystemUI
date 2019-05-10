LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_USE_AAPT2 := true

LOCAL_MODULE_TAGS := optional

LOCAL_MODULE := AirLib

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
LOCAL_JAR_EXCLUDE_FILES := none

include $(BUILD_STATIC_JAVA_LIBRARY)

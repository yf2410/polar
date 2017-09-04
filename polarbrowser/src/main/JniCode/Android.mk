# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := native
LOCAL_SRC_FILES := AdblockInvoke.cpp \
                   AdblockPlus.cpp \
                   exwchar.cpp \
                   jsoncpp.cpp \
                   StringConvertor.cpp \
                   NavigateQuery.cpp \
                   NativeEntry.cpp \
                   NavigateQueryInvoke.cpp \
                   Util.cpp

#LOCAL_LDLIBS :=-llog

include $(BUILD_SHARED_LIBRARY)
#LOCAL_CPP_FEATURES += exceptions
LOCAL_CXXFLAGS += -D_GLIBCXX_USE_WCHAR_T
LOCAL_CFLAGS += -D_GLIBCXX_USE_WCHAR_T
LOCAL_C_INCLUDES := $(LOCAL_PATH)/json

include $(CLEAR_VARS)
LOCAL_MODULE := locSDK5
LOCAL_SRC_FILES := liblocSDK5.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := weibosdkcore
LOCAL_SRC_FILES := libweibosdkcore.so
include $(PREBUILT_SHARED_LIBRARY)
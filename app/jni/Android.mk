#CFLAGS += -std=c11 -Wall -Wextra -pedantic -Werror
#PREFIX ?= /data/data/com.termux/files/usr
#termux-api: termux-api.c
#install: termux-api
#	mkdir -p $(PREFIX)/bin/ $(PREFIX)/libexec/
#	install termux-api $(PREFIX)/libexec/
#	install scripts/* $(PREFIX)/bin/
#.PHONY: install
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := adbshellkit-api
LOCAL_SRC_FILES := adbshellkit-api.c

include $(BUILD_EXECUTABLE)
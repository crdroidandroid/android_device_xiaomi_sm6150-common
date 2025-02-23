#!/vendor/bin/sh
#
# Copyright (C) 2022 Paranoid Android
# Copyright (C) 2025 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

fix_applied=$(getprop persist.vendor.audio.dolby_fix_applied)

if [ "$fix_applied" != "true" ]; then
    # Remove /data/vendor/dolby
    rm -rf /data/vendor/dolby/*
    setprop persist.vendor.audio.dolby_fix_applied true
fi

#!/usr/bin/env bash

# Copyright (c) 2025 Element Creations Ltd.
# Copyright 2025 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

# Ref: https://developer.android.com/training/monitoring-device-state/doze-standby#testing_doze

echo " => Device state"

set -x
adb shell dumpsys deviceidle get light
adb shell dumpsys deviceidle get deep
adb shell dumpsys deviceidle get force
adb shell dumpsys deviceidle get screen
adb shell dumpsys deviceidle get charging
adb shell dumpsys deviceidle get network

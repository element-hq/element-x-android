#!/usr/bin/env bash

# Copyright 2025 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
# Please see LICENSE files in the repository root for full details.

# Ref: https://developer.android.com/training/monitoring-device-state/doze-standby#testing_doze

echo " => Enable doze mode"

set -x
adb shell dumpsys battery unplug
adb shell dumpsys deviceidle force-idle

tools/adb/print_device_state.sh

#!/usr/bin/env bash

# Copyright 2025 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
# Please see LICENSE files in the repository root for full details.

# Ref: https://developer.android.com/training/monitoring-device-state/doze-standby#testing_doze

echo " => Disable doze mode"

set -x
adb shell dumpsys deviceidle unforce
adb shell dumpsys battery reset

tools/adb/print_device_state.sh

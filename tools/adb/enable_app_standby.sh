#!/usr/bin/env bash

# Copyright (c) 2025 Element Creations Ltd.
# Copyright 2025 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

# Ref: https://developer.android.com/training/monitoring-device-state/doze-standby#testing_your_app_with_app_standby

echo " => Standby ON"

set -x
package_name="io.element.android.x.debug"
adb shell dumpsys battery unplug
adb shell am set-inactive "${package_name}" true
adb shell am get-inactive "${package_name}"

tools/adb/print_device_state.sh

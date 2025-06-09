#! /bin/bash

# Copyright 2025 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
# Please see LICENSE files in the repository root for full details.

adb shell am start -a android.intent.action.VIEW \
   -d "matrix:r/element-android:matrix.org"

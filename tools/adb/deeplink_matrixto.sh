#! /bin/bash

# Copyright 2024 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only
# Please see LICENSE in the repository root for full details.

adb shell am start -a android.intent.action.VIEW \
   -d "element://room/%23element-android%3Amatrix.org"

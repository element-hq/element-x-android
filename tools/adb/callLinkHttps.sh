#! /bin/bash

# Copyright 2023-2024 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only
# Please see LICENSE in the repository root for full details.

# Format is:
# https://call.element.io/*
# For instance
# https://call.element.io/TestElementCall

adb shell am start -a android.intent.action.VIEW -d https://call.element.io/TestElementCall

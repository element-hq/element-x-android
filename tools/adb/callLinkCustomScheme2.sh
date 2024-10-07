#! /bin/bash

# Copyright 2023-2024 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only
# Please see LICENSE in the repository root for full details.

# Format is:
# io.element.call:/?url=some-encoded-url
# For instance
# io.element.call:/?url=https%3A%2F%2Fcall.element.io%2FTestElementCall

adb shell am start -a android.intent.action.VIEW -d io.element.call:/?url=https%3A%2F%2Fcall.element.io%2FTestElementCall

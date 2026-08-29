#!/usr/bin/env bash

# Copyright (c) 2026 Element Creations Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

package_name="io.element.android.x.debug"

echo " => Force stop ${package_name}"

adb shell am force-stop "${package_name}"

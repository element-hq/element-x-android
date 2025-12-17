#! /bin/bash

# Copyright (c) 2025 Element Creations Ltd.
# Copyright 2023-2024 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

# Format is:

# Error
# adb shell am start -a android.intent.action.VIEW -d "io.element.android:/?error=access_denied\\&state=IFF1UETGye2ZA8pO"

# Success
adb shell am start -a android.intent.action.VIEW -d "io.element.android:/?state=IFF1UETGye2ZA8pO\\&code=y6X1GZeqA3xxOWcTeShgv8nkgFJXyzWB"

#! /bin/bash

# Copyright 2023-2024 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only
# Please see LICENSE in the repository root for full details.

# Format is:

# Error
# adb shell am start -a android.intent.action.VIEW -d "io.element:/callback?error=access_denied\\&state=IFF1UETGye2ZA8pO"

# Success
adb shell am start -a android.intent.action.VIEW -d "io.element:/callback?state=IFF1UETGye2ZA8pO\\&code=y6X1GZeqA3xxOWcTeShgv8nkgFJXyzWB"

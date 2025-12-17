#! /bin/bash

# Copyright (c) 2025 Element Creations Ltd.
# Copyright 2025 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

# Format is:
# https://mobile.element.io/element/?account_provider=example.org&login_hint=mxid:@alice:example.org

adb shell am start -a android.intent.action.VIEW \
    -d "https://mobile.element.io/element/?account_provider=element.io\\&login_hint=mxid:@alice:element.io"

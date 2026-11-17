#! /bin/bash

# Copyright (c) 2025 Element Creations Ltd.
# Copyright 2023-2024 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

# Simulate a notification click.
# Format is:
# elementx://open/{sessionId} to open a session
# elementx://open/{sessionId}/{roomId} to open a room
# elementx://open/{sessionId}/{roomId}/{threadId} to open a thread
#
# There is no intent-filter for this scheme, the Intent has to be explicit, so the component must be provided.
# Adapt the package name to the installed variant (e.g. io.element.android.x, io.element.android.x.debug, io.element.android.x.nightly).
PACKAGE="io.element.android.x.debug"
COMPONENT="${PACKAGE}/io.element.android.x.MainActivity"

# Open a session
# adb shell am start -n ${COMPONENT} -a android.intent.action.VIEW -d elementx://open/@benoit10518:matrix.org
# Open a room
adb shell am start -n ${COMPONENT} -a android.intent.action.VIEW -d elementx://open/@benoit10518:matrix.org/!dehdDVSkabQLZFYrgo:matrix.org
# Open a thread
# adb shell am start -n ${COMPONENT} -a android.intent.action.VIEW -d elementx://open/@benoit10518:matrix.org/!dehdDVSkabQLZFYrgo:matrix.org/\\\$threadId

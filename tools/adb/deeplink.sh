#! /bin/bash

# Copyright 2023-2024 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only
# Please see LICENSE in the repository root for full details.

# Format is:
# elementx://open/{sessionId} to open a session
# elementx://open/{sessionId}/{roomId} to open a room
# elementx://open/{sessionId}/{roomId}/{eventId} to open a thread

# Open a session
# adb shell am start -a android.intent.action.VIEW -d elementx://open/@benoit10518:matrix.org
# Open a room
adb shell am start -a android.intent.action.VIEW -d elementx://open/@benoit10518:matrix.org/!dehdDVSkabQLZFYrgo:matrix.org
# Open a thread
# adb shell am start -a android.intent.action.VIEW -d elementx://open/@benoit10518:matrix.org/!dehdDVSkabQLZFYrgo:matrix.org/\\\$threadId

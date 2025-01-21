/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.factories

import org.matrix.rustcomponents.sdk.RoomNotificationMode
import org.matrix.rustcomponents.sdk.RoomNotificationSettings

fun aRustRoomNotificationSettings(
    mode: RoomNotificationMode = RoomNotificationMode.ALL_MESSAGES,
    isDefault: Boolean = true,
) = RoomNotificationSettings(
    mode = mode,
    isDefault = isDefault
)

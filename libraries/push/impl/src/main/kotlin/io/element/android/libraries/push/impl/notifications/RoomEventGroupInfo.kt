/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId

/**
 * Data class to hold information about a group of notifications for a room.
 */
data class RoomEventGroupInfo(
    val sessionId: SessionId,
    val roomId: RoomId,
    val roomDisplayName: String,
    val isDm: Boolean = false,
    // An event in the list has not yet been display
    val hasNewEvent: Boolean = false,
    // true if at least one on the not yet displayed event is noisy
    val shouldBing: Boolean = false,
    val customSound: String? = null,
    val hasSmartReplyError: Boolean = false,
    val isUpdated: Boolean = false,
)

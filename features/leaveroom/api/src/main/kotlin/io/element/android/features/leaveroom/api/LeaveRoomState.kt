/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.leaveroom.api

import androidx.compose.runtime.Immutable

/**
 * State of the leave room flow, which is only the confirmation dialogs: the screen it is hosted in owns the rest of the UI.
 */
@Immutable
interface LeaveRoomState {
    /** Where the host screen sends its events to start or confirm leaving a room. */
    val eventSink: (LeaveRoomEvent) -> Unit
}

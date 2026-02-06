/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.messages

import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal providing access to the RoomMemberProfilesCache.
 * This allows composables in the timeline to access nicknames and other cached member data.
 */
val LocalRoomMemberProfilesCache = compositionLocalOf<RoomMemberProfilesCache?> { null }

/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.api

import kotlinx.coroutines.flow.Flow

/** Additional build-specific eligibility for the room call shortcut. */
fun interface RoomCallActionPolicy {
    fun isCallActionEnabledFlow(): Flow<Boolean>
}

/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl

import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.roomdetails.api.RoomCallActionPolicy
import io.element.android.libraries.di.RoomScope
import kotlinx.coroutines.flow.flowOf

@ContributesBinding(RoomScope::class)
class NoopRoomCallActionPolicy : RoomCallActionPolicy {
    override fun isCallActionEnabledFlow() = flowOf(true)
}

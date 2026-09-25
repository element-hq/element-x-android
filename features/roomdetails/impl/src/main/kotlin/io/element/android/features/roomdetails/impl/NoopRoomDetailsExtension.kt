/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.roomdetails.api.RoomDetailsExtension
import io.element.android.libraries.di.RoomScope

@ContributesBinding(RoomScope::class)
class NoopRoomDetailsExtension : RoomDetailsExtension {
    @Composable
    override fun Render(modifier: Modifier) = Unit
}

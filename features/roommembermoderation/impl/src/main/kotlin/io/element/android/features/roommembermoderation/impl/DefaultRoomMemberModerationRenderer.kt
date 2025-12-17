/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roommembermoderation.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.roommembermoderation.api.ModerationAction
import io.element.android.features.roommembermoderation.api.RoomMemberModerationRenderer
import io.element.android.features.roommembermoderation.api.RoomMemberModerationState
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.user.MatrixUser
import timber.log.Timber

@ContributesBinding(RoomScope::class)
class DefaultRoomMemberModerationRenderer : RoomMemberModerationRenderer {
    @Composable
    override fun Render(
        state: RoomMemberModerationState,
        onSelectAction: (ModerationAction, MatrixUser) -> Unit,
        modifier: Modifier
    ) {
        if (state is InternalRoomMemberModerationState) {
            RoomMemberModerationView(state, onSelectAction, modifier)
        } else {
            SideEffect {
                Timber.d("RoomMemberModerationRenderer: Render called with unsupported state: $state")
            }
        }
    }
}

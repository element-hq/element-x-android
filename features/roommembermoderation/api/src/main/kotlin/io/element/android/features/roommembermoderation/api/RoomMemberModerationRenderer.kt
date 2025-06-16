/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roommembermoderation.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.libraries.matrix.api.user.MatrixUser

interface RoomMemberModerationRenderer {
    @Composable
    fun Render(
        state: RoomMemberModerationState,
        onSelectAction: (ModerationAction, MatrixUser) -> Unit,
        modifier: Modifier,
    )
}

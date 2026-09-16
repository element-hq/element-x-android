/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roommembermoderation.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.libraries.matrix.api.user.MatrixUser

/**
 * Renders the moderation action sheet and its confirmation dialogs; it draws nothing until the host screen asks for a member's actions.
 */
interface RoomMemberModerationRenderer {
    /**
     * Draws whichever sheet or dialog the current state calls for.
     *
     * @param state the state produced by the moderation presenter.
     * @param onSelectAction called when the user picks an action, so the host screen can navigate if that action needs its own screen.
     * @param onAvatarClick called when the user taps the member's avatar, only if the user has an avatar.
     * @param modifier layout modifier for the container.
     */
    @Composable
    fun Render(
        state: RoomMemberModerationState,
        onSelectAction: (ModerationAction, MatrixUser) -> Unit,
        onAvatarClick: (MatrixUser) -> Unit,
        modifier: Modifier,
    )
}

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.impl.acceptdecline

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteState
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteView
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId

@ContributesBinding(SessionScope::class)
class DefaultAcceptDeclineInviteView : AcceptDeclineInviteView {
    @Composable
    override fun Render(
        state: AcceptDeclineInviteState,
        onAcceptInviteSuccess: (RoomId) -> Unit,
        onDeclineInviteSuccess: (RoomId) -> Unit,
        modifier: Modifier,
    ) {
        AcceptDeclineInviteView(
            state = state,
            onAcceptInviteSuccess = onAcceptInviteSuccess,
            onDeclineInviteSuccess = onDeclineInviteSuccess,
            modifier = modifier
        )
    }
}

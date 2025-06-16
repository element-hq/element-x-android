/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.api.acceptdecline

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.libraries.matrix.api.core.RoomId

interface AcceptDeclineInviteView {
    @Composable
    fun Render(
        state: AcceptDeclineInviteState,
        onAcceptInviteSuccess: (RoomId) -> Unit,
        onDeclineInviteSuccess: (RoomId) -> Unit,
        modifier: Modifier,
    )
}

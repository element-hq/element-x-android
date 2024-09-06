/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.invite.api.response

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.libraries.matrix.api.core.RoomId

interface AcceptDeclineInviteView {
    @Composable
    fun Render(
        state: AcceptDeclineInviteState,
        onAcceptInvite: (RoomId) -> Unit,
        onDeclineInvite: (RoomId) -> Unit,
        modifier: Modifier,
    )
}

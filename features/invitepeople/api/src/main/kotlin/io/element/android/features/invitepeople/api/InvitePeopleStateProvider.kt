/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invitepeople.api

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class InvitePeopleStateProvider : PreviewParameterProvider<InvitePeopleState> {
    override val values: Sequence<InvitePeopleState>
        get() = sequenceOf(
            PreviewInvitePeopleState(),
            PreviewInvitePeopleState(canInvite = true),
            PreviewInvitePeopleState(isSearchActive = true)
        )
}

private data class PreviewInvitePeopleState(
    override val canInvite: Boolean = false,
    override val isSearchActive: Boolean = false,
    override val eventSink: (InvitePeopleEvents) -> Unit = {}
) : InvitePeopleState

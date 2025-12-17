/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invitepeople.api

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction

class InvitePeopleStateProvider : PreviewParameterProvider<InvitePeopleState> {
    override val values: Sequence<InvitePeopleState>
        get() = sequenceOf(
            aPreviewInvitePeopleState(),
            aPreviewInvitePeopleState(canInvite = true),
            aPreviewInvitePeopleState(isSearchActive = true),
            aPreviewInvitePeopleState(sendInvitesAction = AsyncAction.Loading),
        )
}

private data class PreviewInvitePeopleState(
    override val canInvite: Boolean,
    override val isSearchActive: Boolean,
    override val sendInvitesAction: AsyncAction<Unit>,
    override val eventSink: (InvitePeopleEvents) -> Unit,
) : InvitePeopleState

private fun aPreviewInvitePeopleState(
    canInvite: Boolean = false,
    isSearchActive: Boolean = false,
    sendInvitesAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (InvitePeopleEvents) -> Unit = {},
) = PreviewInvitePeopleState(
    canInvite = canInvite,
    isSearchActive = isSearchActive,
    sendInvitesAction = sendInvitesAction,
    eventSink = eventSink
)

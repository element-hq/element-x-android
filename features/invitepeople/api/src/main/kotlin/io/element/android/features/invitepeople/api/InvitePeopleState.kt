/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invitepeople.api

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId

/**
 * State of the invite people UI, produced by an [InvitePeoplePresenter] and consumed by an [InvitePeopleRenderer].
 */
interface InvitePeopleState {
    /** Whether the send button should be enabled, i.e. at least one user is selected and the user has the permission. */
    val canInvite: Boolean

    /** Whether the search field currently has focus, which the host screen uses to adapt its own layout. */
    val isSearchActive: Boolean

    /** Progress of the invite request, so the UI can show a loader and surface failures. */
    val sendInvitesAction: AsyncAction<Unit>

    /** Progress of turning a direct message into a room, which happens when inviting a third person into a DM. */
    val createRoomFromDmAction: AsyncAction<RoomId>

    /** Where the UI sends its events. */
    val eventSink: (InvitePeopleEvent) -> Unit
}

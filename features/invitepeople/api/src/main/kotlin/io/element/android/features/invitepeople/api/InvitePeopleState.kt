/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invitepeople.api

import io.element.android.libraries.architecture.AsyncAction

interface InvitePeopleState {
    val canInvite: Boolean
    val isSearchActive: Boolean
    val sendInvitesAction: AsyncAction<Unit>
    val eventSink: (InvitePeopleEvents) -> Unit
}

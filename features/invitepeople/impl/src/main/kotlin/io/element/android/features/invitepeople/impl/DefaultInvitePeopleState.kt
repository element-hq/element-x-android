/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invitepeople.impl

import io.element.android.features.invitepeople.api.InvitePeopleEvents
import io.element.android.features.invitepeople.api.InvitePeopleState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList

data class DefaultInvitePeopleState(
    val room: AsyncData<Unit>,
    override val canInvite: Boolean,
    val searchQuery: String,
    val showSearchLoader: Boolean,
    val searchResults: SearchBarResultState<ImmutableList<InvitableUser>>,
    val selectedUsers: ImmutableList<MatrixUser>,
    override val isSearchActive: Boolean,
    override val sendInvitesAction: AsyncAction<Unit>,
    override val eventSink: (InvitePeopleEvents) -> Unit
) : InvitePeopleState

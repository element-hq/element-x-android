/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invitepeople.impl

import io.element.android.features.invitepeople.api.InvitePeopleEvents
import io.element.android.libraries.matrix.api.user.MatrixUser

sealed interface DefaultInvitePeopleEvents : InvitePeopleEvents {
    data class ToggleUser(val user: MatrixUser) : DefaultInvitePeopleEvents
    data class UpdateSearchQuery(val query: String) : DefaultInvitePeopleEvents
    data class OnSearchActiveChanged(val active: Boolean) : DefaultInvitePeopleEvents
}

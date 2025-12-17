/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.impl.declineandblock

sealed interface DeclineAndBlockEvents {
    data class UpdateReportReason(val reason: String) : DeclineAndBlockEvents
    data object ToggleReportRoom : DeclineAndBlockEvents
    data object ToggleBlockUser : DeclineAndBlockEvents
    data object Decline : DeclineAndBlockEvents
    data object ClearDeclineAction : DeclineAndBlockEvents
}

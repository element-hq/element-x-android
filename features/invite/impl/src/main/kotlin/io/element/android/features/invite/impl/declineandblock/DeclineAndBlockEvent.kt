/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.impl.declineandblock

sealed interface DeclineAndBlockEvent {
    data class UpdateReportReason(val reason: String) : DeclineAndBlockEvent
    data object ToggleReportRoom : DeclineAndBlockEvent
    data object ToggleBlockUser : DeclineAndBlockEvent
    data object Decline : DeclineAndBlockEvent
    data object ClearDeclineAction : DeclineAndBlockEvent
}

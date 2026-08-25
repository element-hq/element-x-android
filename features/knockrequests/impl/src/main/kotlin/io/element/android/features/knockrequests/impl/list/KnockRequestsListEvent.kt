/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.list

import io.element.android.features.knockrequests.impl.data.KnockRequestPresentable

sealed interface KnockRequestsListEvent {
    data class Accept(val knockRequest: KnockRequestPresentable) : KnockRequestsListEvent
    data class Decline(val knockRequest: KnockRequestPresentable) : KnockRequestsListEvent
    data class DeclineAndBan(val knockRequest: KnockRequestPresentable) : KnockRequestsListEvent
    data object AcceptAll : KnockRequestsListEvent
    data object ResetCurrentAction : KnockRequestsListEvent
    data object RetryCurrentAction : KnockRequestsListEvent
    data object ConfirmCurrentAction : KnockRequestsListEvent
}

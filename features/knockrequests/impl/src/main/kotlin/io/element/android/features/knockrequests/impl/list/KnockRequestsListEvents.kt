/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.list

import io.element.android.features.knockrequests.impl.KnockRequest

sealed interface KnockRequestsListEvents {
    data class Accept(val knockRequest: KnockRequest) : KnockRequestsListEvents
    data class Decline(val knockRequest: KnockRequest) : KnockRequestsListEvents
    data object AcceptAll : KnockRequestsListEvents
    data object DismissCurrentAction : KnockRequestsListEvents
}

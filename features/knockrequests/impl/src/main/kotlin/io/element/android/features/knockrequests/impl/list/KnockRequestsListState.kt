/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.list

import io.element.android.features.knockrequests.impl.KnockRequest
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import kotlinx.collections.immutable.ImmutableList

data class KnockRequestsListState(
    val knockRequests: AsyncData<ImmutableList<KnockRequest>>,
    val currentAction: KnockRequestsCurrentAction,
    val eventSink: (KnockRequestsListEvents) -> Unit,
)

sealed interface KnockRequestsCurrentAction {
    data object None : KnockRequestsCurrentAction
    data class Accept(val async: AsyncAction<Unit>) : KnockRequestsCurrentAction
    data class Decline(val async: AsyncAction<Unit>) : KnockRequestsCurrentAction
    data class AcceptAll(val async: AsyncAction<Unit>) : KnockRequestsCurrentAction
}

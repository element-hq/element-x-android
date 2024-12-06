/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.list

import androidx.compose.runtime.Immutable
import io.element.android.features.knockrequests.impl.KnockRequest
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import kotlinx.collections.immutable.ImmutableList

data class KnockRequestsListState(
    val knockRequests: AsyncData<ImmutableList<KnockRequest>>,
    val currentAction: KnockRequestsCurrentAction,
    val canAccept: Boolean,
    val canDecline: Boolean,
    val canBan: Boolean,
    val eventSink: (KnockRequestsListEvents) -> Unit,
) {
    val canAcceptAll = knockRequests is AsyncData.Success && knockRequests.data.size > 1
}

@Immutable
sealed interface KnockRequestsCurrentAction {
    data object None : KnockRequestsCurrentAction
    data class Accept(val knockRequest: KnockRequest, val async: AsyncAction<Unit>) : KnockRequestsCurrentAction
    data class Decline(val knockRequest: KnockRequest, val async: AsyncAction<Unit>) : KnockRequestsCurrentAction
    data class DeclineAndBan(val knockRequest: KnockRequest, val async: AsyncAction<Unit>) : KnockRequestsCurrentAction
    data class AcceptAll(val async: AsyncAction<Unit>) : KnockRequestsCurrentAction
}

/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.list

import androidx.compose.runtime.Immutable
import io.element.android.features.knockrequests.impl.data.KnockRequestPermissions
import io.element.android.features.knockrequests.impl.data.KnockRequestPresentable
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import kotlinx.collections.immutable.ImmutableList

data class KnockRequestsListState(
    val knockRequests: AsyncData<ImmutableList<KnockRequestPresentable>>,
    val currentAction: KnockRequestsAction,
    val asyncAction: AsyncAction<Unit>,
    val permissions: KnockRequestPermissions,
    val eventSink: (KnockRequestsListEvents) -> Unit,
) {
    val canAcceptAll = permissions.canAccept && knockRequests is AsyncData.Success && knockRequests.data.size > 1
}

@Immutable
sealed interface KnockRequestsAction {
    data object None : KnockRequestsAction
    data class Accept(val knockRequest: KnockRequestPresentable) : KnockRequestsAction
    data class Decline(val knockRequest: KnockRequestPresentable) : KnockRequestsAction
    data class DeclineAndBan(val knockRequest: KnockRequestPresentable) : KnockRequestsAction
    data object AcceptAll : KnockRequestsAction
}

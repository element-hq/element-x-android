/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.element.android.features.knockrequests.impl.data.KnockRequestsService
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class KnockRequestsListPresenter @Inject constructor(
    private val knockRequestsService: KnockRequestsService,
) : Presenter<KnockRequestsListState> {
    @Composable
    override fun present(): KnockRequestsListState {
        val asyncAction = remember { mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized) }
        var actionTarget by remember { mutableStateOf<KnockRequestsActionTarget>(KnockRequestsActionTarget.None) }

        val permissions by knockRequestsService.permissionsFlow.collectAsState()
        val knockRequests by knockRequestsService.knockRequestsFlow.collectAsState()

        val coroutineScope = rememberCoroutineScope()

        fun handleEvents(event: KnockRequestsListEvents) {
            when (event) {
                KnockRequestsListEvents.AcceptAll -> {
                    actionTarget = KnockRequestsActionTarget.AcceptAll
                }
                is KnockRequestsListEvents.Accept -> {
                    actionTarget = KnockRequestsActionTarget.Accept(event.knockRequest)
                }
                is KnockRequestsListEvents.Decline -> {
                    actionTarget = KnockRequestsActionTarget.Decline(event.knockRequest)
                }
                is KnockRequestsListEvents.DeclineAndBan -> {
                    actionTarget = KnockRequestsActionTarget.DeclineAndBan(event.knockRequest)
                }
                KnockRequestsListEvents.ResetCurrentAction -> {
                    asyncAction.value = AsyncAction.Uninitialized
                    actionTarget = KnockRequestsActionTarget.None
                }
                KnockRequestsListEvents.RetryCurrentAction -> {
                    coroutineScope.executeAction(actionTarget, asyncAction, isActionConfirmed = true)
                }
                KnockRequestsListEvents.ConfirmCurrentAction -> {
                    coroutineScope.executeAction(actionTarget, asyncAction, isActionConfirmed = true)
                }
            }
        }
        LaunchedEffect(actionTarget) {
            executeAction(actionTarget, asyncAction, isActionConfirmed = false)
        }

        return KnockRequestsListState(
            knockRequests = knockRequests,
            actionTarget = actionTarget,
            permissions = permissions,
            asyncAction = asyncAction.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.executeAction(
        actionTarget: KnockRequestsActionTarget,
        asyncAction: MutableState<AsyncAction<Unit>>,
        isActionConfirmed: Boolean,
    ) = launch {
        when (actionTarget) {
            is KnockRequestsActionTarget.Accept -> {
                runUpdatingState(asyncAction) {
                    knockRequestsService.acceptKnockRequest(actionTarget.knockRequest)
                }
            }
            is KnockRequestsActionTarget.Decline -> {
                if (isActionConfirmed) {
                    runUpdatingState(asyncAction) {
                        knockRequestsService.declineKnockRequest(actionTarget.knockRequest)
                    }
                } else {
                    asyncAction.value = AsyncAction.ConfirmingNoParams
                }
            }
            is KnockRequestsActionTarget.DeclineAndBan -> {
                if (isActionConfirmed) {
                    runUpdatingState(asyncAction) {
                        knockRequestsService.declineAndBanKnockRequest(actionTarget.knockRequest)
                    }
                } else {
                    asyncAction.value = AsyncAction.ConfirmingNoParams
                }
            }
            is KnockRequestsActionTarget.AcceptAll -> {
                if (isActionConfirmed) {
                    runUpdatingState(asyncAction) {
                        knockRequestsService.acceptAllKnockRequests()
                    }
                } else {
                    asyncAction.value = AsyncAction.ConfirmingNoParams
                }
            }
            KnockRequestsActionTarget.None -> Unit
        }
    }
}

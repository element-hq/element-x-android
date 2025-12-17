/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
import dev.zacsweers.metro.Inject
import io.element.android.features.knockrequests.impl.data.KnockRequestsService
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Inject
class KnockRequestsListPresenter(
    private val knockRequestsService: KnockRequestsService,
) : Presenter<KnockRequestsListState> {
    @Composable
    override fun present(): KnockRequestsListState {
        val asyncAction = remember { mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized) }
        var currentAction by remember { mutableStateOf<KnockRequestsAction>(KnockRequestsAction.None) }

        val permissions by knockRequestsService.permissionsFlow.collectAsState()
        val knockRequests by knockRequestsService.knockRequestsFlow.collectAsState()

        val coroutineScope = rememberCoroutineScope()

        fun handleEvent(event: KnockRequestsListEvents) {
            when (event) {
                KnockRequestsListEvents.AcceptAll -> {
                    currentAction = KnockRequestsAction.AcceptAll
                }
                is KnockRequestsListEvents.Accept -> {
                    currentAction = KnockRequestsAction.Accept(event.knockRequest)
                }
                is KnockRequestsListEvents.Decline -> {
                    currentAction = KnockRequestsAction.Decline(event.knockRequest)
                }
                is KnockRequestsListEvents.DeclineAndBan -> {
                    currentAction = KnockRequestsAction.DeclineAndBan(event.knockRequest)
                }
                KnockRequestsListEvents.ResetCurrentAction -> {
                    asyncAction.value = AsyncAction.Uninitialized
                    currentAction = KnockRequestsAction.None
                }
                KnockRequestsListEvents.RetryCurrentAction -> {
                    coroutineScope.executeAction(currentAction, asyncAction, isActionConfirmed = true)
                }
                KnockRequestsListEvents.ConfirmCurrentAction -> {
                    coroutineScope.executeAction(currentAction, asyncAction, isActionConfirmed = true)
                }
            }
        }
        LaunchedEffect(currentAction) {
            executeAction(currentAction, asyncAction, isActionConfirmed = false)
        }

        return KnockRequestsListState(
            knockRequests = knockRequests,
            currentAction = currentAction,
            permissions = permissions,
            asyncAction = asyncAction.value,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.executeAction(
        currentAction: KnockRequestsAction,
        asyncAction: MutableState<AsyncAction<Unit>>,
        isActionConfirmed: Boolean,
    ) = launch {
        when (currentAction) {
            is KnockRequestsAction.Accept -> {
                runUpdatingState(asyncAction) {
                    knockRequestsService.acceptKnockRequest(currentAction.knockRequest)
                }
            }
            is KnockRequestsAction.Decline -> {
                if (isActionConfirmed) {
                    runUpdatingState(asyncAction) {
                        knockRequestsService.declineKnockRequest(currentAction.knockRequest)
                    }
                } else {
                    asyncAction.value = AsyncAction.ConfirmingNoParams
                }
            }
            is KnockRequestsAction.DeclineAndBan -> {
                if (isActionConfirmed) {
                    runUpdatingState(asyncAction) {
                        knockRequestsService.declineAndBanKnockRequest(currentAction.knockRequest)
                    }
                } else {
                    asyncAction.value = AsyncAction.ConfirmingNoParams
                }
            }
            is KnockRequestsAction.AcceptAll -> {
                if (isActionConfirmed) {
                    runUpdatingState(asyncAction) {
                        knockRequestsService.acceptAllKnockRequests()
                    }
                } else {
                    asyncAction.value = AsyncAction.ConfirmingNoParams
                }
            }
            KnockRequestsAction.None -> Unit
        }
    }
}

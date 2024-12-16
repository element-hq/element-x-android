/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.element.android.features.knockrequests.impl.data.KnockRequestsService
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.ui.room.canBanAsState
import io.element.android.libraries.matrix.ui.room.canInviteAsState
import io.element.android.libraries.matrix.ui.room.canKickAsState
import javax.inject.Inject

class KnockRequestsListPresenter @Inject constructor(
    private val room: MatrixRoom,
    private val knockRequestsService: KnockRequestsService,
) : Presenter<KnockRequestsListState> {
    @Composable
    override fun present(): KnockRequestsListState {
        val asyncAction = remember { mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized) }
        var actionTarget by remember { mutableStateOf<KnockRequestsActionTarget>(KnockRequestsActionTarget.None) }
        var targetActionConfirmed by remember { mutableStateOf(false) }
        var retryCount by remember { mutableIntStateOf(0) }

        val syncUpdateFlow = room.syncUpdateFlow.collectAsState()
        val canBan by room.canBanAsState(syncUpdateFlow.value)
        val canDecline by room.canKickAsState(syncUpdateFlow.value)
        val canAccept by room.canInviteAsState(syncUpdateFlow.value)

        val knockRequests by knockRequestsService.knockRequestsFlow.collectAsState()

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
                    targetActionConfirmed = false
                }
                KnockRequestsListEvents.RetryCurrentAction -> {
                    retryCount++
                }
                KnockRequestsListEvents.ConfirmCurrentAction -> {
                    targetActionConfirmed = true
                }
            }
        }

        LaunchedEffect(actionTarget, targetActionConfirmed, retryCount) {
            when (val action = actionTarget) {
                is KnockRequestsActionTarget.Accept -> {
                    runUpdatingState(asyncAction) {
                        knockRequestsService.acceptKnockRequest(action.knockRequest)
                    }
                }
                is KnockRequestsActionTarget.Decline -> {
                    if (targetActionConfirmed) {
                        runUpdatingState(asyncAction) {
                            knockRequestsService.declineKnockRequest(action.knockRequest)
                        }
                    } else {
                        asyncAction.value = AsyncAction.ConfirmingNoParams
                    }
                }
                is KnockRequestsActionTarget.DeclineAndBan -> {
                    if (targetActionConfirmed) {
                        runUpdatingState(asyncAction) {
                            knockRequestsService.declineAndBanKnockRequest(action.knockRequest)
                        }
                    } else {
                        asyncAction.value = AsyncAction.ConfirmingNoParams
                    }
                }
                is KnockRequestsActionTarget.AcceptAll -> {
                    if (targetActionConfirmed) {
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

        return KnockRequestsListState(
            knockRequests = knockRequests,
            actionTarget = actionTarget,
            asyncAction = asyncAction.value,
            canAccept = canAccept,
            canDecline = canDecline,
            canBan = canBan,
            eventSink = ::handleEvents
        )
    }
}

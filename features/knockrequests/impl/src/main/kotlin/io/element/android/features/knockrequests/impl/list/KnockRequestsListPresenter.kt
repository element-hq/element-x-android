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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.ui.room.canBanAsState
import io.element.android.libraries.matrix.ui.room.canInviteAsState
import io.element.android.libraries.matrix.ui.room.canKickAsState
import kotlinx.collections.immutable.persistentListOf
import javax.inject.Inject

class KnockRequestsListPresenter @Inject constructor(
    private val room: MatrixRoom,
) : Presenter<KnockRequestsListState> {

    @Composable
    override fun present(): KnockRequestsListState {
        val currentAction = remember { mutableStateOf<KnockRequestsCurrentAction>(KnockRequestsCurrentAction.None) }
        val syncUpdateFlow = room.syncUpdateFlow.collectAsState()
        val canBan by room.canBanAsState(syncUpdateFlow.value)
        val canDecline by room.canKickAsState(syncUpdateFlow.value)
        val canAccept by room.canInviteAsState(syncUpdateFlow.value)

        fun handleEvents(event: KnockRequestsListEvents) {
            when (event) {
                KnockRequestsListEvents.AcceptAll -> {
                    currentAction.value = KnockRequestsCurrentAction.AcceptAll(AsyncAction.Uninitialized)
                }
                is KnockRequestsListEvents.Accept -> {
                    currentAction.value = KnockRequestsCurrentAction.Accept(event.knockRequest, AsyncAction.Uninitialized)
                }
                is KnockRequestsListEvents.Decline -> {
                    currentAction.value = KnockRequestsCurrentAction.Decline(event.knockRequest, AsyncAction.Uninitialized)
                }
                is KnockRequestsListEvents.DeclineAndBan -> {
                    currentAction.value = KnockRequestsCurrentAction.DeclineAndBan(event.knockRequest, AsyncAction.Uninitialized)
                }
                KnockRequestsListEvents.DismissCurrentAction -> {
                    currentAction.value = KnockRequestsCurrentAction.None
                }
            }
        }

        LaunchedEffect(currentAction) {
            when (val action = currentAction.value) {
                is KnockRequestsCurrentAction.Accept -> {
                    // Accept the knock request
                }
                is KnockRequestsCurrentAction.Decline -> {
                    // Decline the knock request
                }
                is KnockRequestsCurrentAction.DeclineAndBan -> {
                    // Decline and ban the user
                }
                is KnockRequestsCurrentAction.AcceptAll -> {
                    // Accept all knock requests
                }
                KnockRequestsCurrentAction.None -> Unit
            }
        }

        return KnockRequestsListState(
            knockRequests = AsyncData.Success(persistentListOf()),
            currentAction = currentAction.value,
            canAccept = canAccept,
            canDecline = canDecline,
            canBan = canBan,
            eventSink = ::handleEvents
        )
    }
}

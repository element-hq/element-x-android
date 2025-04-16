/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.impl.declineandblock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.invite.api.acceptdecline.InviteData
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.matrix.api.MatrixClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DeclineAndBlockPresenter @AssistedInject constructor(
    @Assisted private val inviteData: InviteData,
    private val client: MatrixClient,
) : Presenter<DeclineAndBlockState> {

    @AssistedFactory
    interface Factory {
        fun create(inviteData: InviteData): DeclineAndBlockPresenter
    }

    @Composable
    override fun present(): DeclineAndBlockState {
        var reportReason by rememberSaveable { mutableStateOf("") }
        var blockUser by rememberSaveable { mutableStateOf(true) }
        var reportRoom by rememberSaveable { mutableStateOf(false) }
        var declineAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }

        val coroutineScope = rememberCoroutineScope()

        fun handleEvents(event: DeclineAndBlockEvents) {
            when (event) {
                DeclineAndBlockEvents.ClearDeclineAction -> declineAction.value = AsyncAction.Uninitialized
                DeclineAndBlockEvents.Decline -> coroutineScope.decline(reportReason, blockUser, reportRoom, declineAction)
                DeclineAndBlockEvents.ToggleBlockUser -> blockUser = !blockUser
                DeclineAndBlockEvents.ToggleReportRoom -> reportRoom = !reportRoom
                is DeclineAndBlockEvents.UpdateReportReason -> reportReason = event.reason
            }
        }

        return DeclineAndBlockState(
            reportRoom = reportRoom,
            reportReason = reportReason,
            blockUser = blockUser,
            declineAction = declineAction.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.decline(
        reason: String,
        blockUser: Boolean,
        reportRoom: Boolean,
        action: MutableState<AsyncAction<Unit>>
    ) = launch {
        runUpdatingState(action) {
            runCatching {
                client.getPendingRoom(inviteData.roomId)!!.use {
                    it.leave().getOrThrow()
                }
                if (blockUser) {
                    client.ignoreUser(inviteData.senderId).getOrThrow()
                }
                if(reportRoom) {
                    //room.reportRoom(reason.takeIf { it.isNotBlank() }).getOrThrow()
                }
            }
        }
    }
}

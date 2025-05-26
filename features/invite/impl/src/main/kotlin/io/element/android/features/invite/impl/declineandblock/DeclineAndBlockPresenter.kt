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
import io.element.android.features.invite.api.InviteData
import io.element.android.features.invite.impl.DeclineInvite
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DeclineAndBlockPresenter @AssistedInject constructor(
    @Assisted private val inviteData: InviteData,
    private val declineInvite: DeclineInvite,
    private val snackbarDispatcher: SnackbarDispatcher,
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
        val declineAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }

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
        action.value = AsyncAction.Loading
        declineInvite(
            roomId = inviteData.roomId,
            blockUser = blockUser,
            reportRoom = reportRoom,
            reportReason = reason
        ).onSuccess {
            action.value = AsyncAction.Success(Unit)
        }.onFailure { error ->
            if (error is DeclineInvite.Exception.DeclineInviteFailed) {
                action.value = AsyncAction.Failure(error)
            } else {
                action.value = AsyncAction.Uninitialized
                snackbarDispatcher.post(SnackbarMessage(CommonStrings.error_unknown))
            }
        }
    }
}

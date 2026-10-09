/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.features.share.api.ShareIntentData
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AssistedInject
class SharePresenter(
    @Assisted private val shareIntentData: ShareIntentData,
    @Assisted initialSessionId: SessionId,
    @AppCoroutineScope
    private val appCoroutineScope: CoroutineScope,
    private val matrixClientProvider: MatrixClientProvider,
    private val shareDataSender: ShareDataSender,
) : Presenter<ShareState> {
    @AssistedFactory
    fun interface Factory {
        fun create(shareIntentData: ShareIntentData, initialSessionId: SessionId): SharePresenter
    }

    private val shareActionState: MutableState<AsyncAction<List<RoomId>>> = mutableStateOf(AsyncAction.Uninitialized)

    /**
     * The session the data is shared with, it can be updated if the user selects rooms from another session.
     */
    private var targetSessionId by mutableStateOf(initialSessionId)

    fun onRoomSelected(sessionId: SessionId, roomIds: List<RoomId>) {
        if (shareActionState.value.isLoading()) return
        targetSessionId = sessionId
        appCoroutineScope.launch {
            suspend {
                val client = matrixClientProvider.getOrRestore(sessionId).getOrThrow()
                shareDataSender.send(client, shareIntentData, roomIds).getOrThrow()
                roomIds
            }.runCatchingUpdatingState(shareActionState)
        }
    }

    @Composable
    override fun present(): ShareState {
        fun handleEvent(event: ShareEvent) {
            when (event) {
                ShareEvent.ClearError -> shareActionState.value = AsyncAction.Uninitialized
            }
        }

        return ShareState(
            sessionId = targetSessionId,
            shareAction = shareActionState.value,
            eventSink = ::handleEvent,
        )
    }
}

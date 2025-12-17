/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.historyvisible

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.history.RoomHistoryVisibility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Inject
class HistoryVisibleStatePresenter(
    private val featureFlagService: FeatureFlagService,
    private val repository: HistoryVisibleAcknowledgementRepository,
    private val room: JoinedRoom,
) : Presenter<HistoryVisibleState> {
    @Composable
    override fun present(): HistoryVisibleState {
        val isFeatureEnabled by featureFlagService.isFeatureEnabledFlow(FeatureFlags.EnableKeyShareOnInvite).collectAsState(initial = false)
        val roomInfo by room.roomInfoFlow.collectAsState()
        // Implicitly assume the alert is initially acknowledged to avoid flashes in UI.
        val acknowledged by repository.hasAcknowledged(room.roomId).collectAsState(initial = true)

        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(roomInfo.historyVisibility, acknowledged) {
            if (roomInfo.historyVisibility == RoomHistoryVisibility.Joined && acknowledged) {
                repository.setAcknowledged(room.roomId, false)
            }
        }

        fun handleEvent(event: HistoryVisibleEvent) {
            when (event) {
                is HistoryVisibleEvent.Acknowledge -> coroutineScope.setAcknowledged(room.roomId, true)
            }
        }

        return HistoryVisibleState(
            showAlert = isFeatureEnabled && roomInfo.historyVisibility != RoomHistoryVisibility.Joined && roomInfo.isEncrypted == true && !acknowledged,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.setAcknowledged(roomId: RoomId, value: Boolean) = launch {
        repository.setAcknowledged(roomId, value)
    }
}

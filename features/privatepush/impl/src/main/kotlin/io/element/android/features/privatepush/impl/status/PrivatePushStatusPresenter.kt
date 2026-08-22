/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.impl.status

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.features.privatepush.api.PrivatePushService
import io.element.android.features.privatepush.api.PrivatePushStatus
import io.element.android.features.privatepush.api.PrivatePushStatusEvents
import io.element.android.features.privatepush.api.PrivatePushStatusState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient

/** Bound as Presenter<PrivatePushStatusState> (SessionScope) and embedded in the upstream notification settings presenter. */
@Inject
class PrivatePushStatusPresenter(
    private val matrixClient: MatrixClient,
    private val privatePushService: PrivatePushService,
) : Presenter<PrivatePushStatusState> {
    @Composable
    override fun present(): PrivatePushStatusState {
        var status by remember { mutableStateOf<AsyncData<PrivatePushStatus>>(AsyncData.Uninitialized) }
        var refresh by remember { mutableIntStateOf(0) }

        LaunchedEffect(refresh) {
            status = AsyncData.Success(privatePushService.status(matrixClient.sessionId))
        }

        fun handleEvent(event: PrivatePushStatusEvents) {
            when (event) {
                PrivatePushStatusEvents.Refresh -> refresh++
            }
        }

        return PrivatePushStatusState(
            status = status,
            eventSink = ::handleEvent,
        )
    }
}

/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.samples.minimal

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import io.element.android.features.invitelist.impl.DefaultSeenInvitesStore
import io.element.android.features.networkmonitor.impl.NetworkMonitorImpl
import io.element.android.features.roomlist.impl.DefaultInviteStateDataSource
import io.element.android.features.roomlist.impl.DefaultRoomLastMessageFormatter
import io.element.android.features.roomlist.impl.RoomListPresenter
import io.element.android.features.roomlist.impl.RoomListView
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.dateformatter.impl.DateFormatters
import io.element.android.libraries.dateformatter.impl.DefaultLastMessageTimestampFormatter
import io.element.android.libraries.dateformatter.impl.LocalDateTimeProvider
import io.element.android.libraries.designsystem.utils.SnackbarDispatcher
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import java.util.Locale
import java.util.concurrent.Executors

class RoomListScreen(
    context: Context,
    private val matrixClient: MatrixClient,
) {
    private val clock = Clock.System
    private val locale = Locale.getDefault()
    private val timeZone = TimeZone.currentSystemDefault()
    private val dateTimeProvider = LocalDateTimeProvider(clock, timeZone)
    private val dateFormatters = DateFormatters(locale, clock, timeZone)
    private val sessionVerificationService = matrixClient.sessionVerificationService()
    private val presenter = RoomListPresenter(
        client = matrixClient,
        lastMessageTimestampFormatter = DefaultLastMessageTimestampFormatter(dateTimeProvider, dateFormatters),
        roomLastMessageFormatter = DefaultRoomLastMessageFormatter(context, matrixClient),
        sessionVerificationService = sessionVerificationService,
        networkMonitor = NetworkMonitorImpl(context),
        snackbarDispatcher = SnackbarDispatcher(),
        inviteStateDataSource = DefaultInviteStateDataSource(
            matrixClient,
            DefaultSeenInvitesStore(context),
            CoroutineDispatchers(
                io = Dispatchers.IO,
                computation = Dispatchers.Default,
                main = Dispatchers.Main,
                diffUpdateDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
            )
        )
    )

    @Composable
    fun Content(modifier: Modifier = Modifier) {
        fun onRoomClicked(roomId: RoomId) {
            val room = matrixClient.getRoom(roomId)!!
            val timeline = room.timeline()
            Singleton.appScope.launch {
                timeline.apply {
                    initialize()
                    paginateBackwards(20, 50)
                    dispose()
                    room.close()
                }
            }
        }

        val state = presenter.present()
        RoomListView(
            state = state,
            modifier = modifier,
            onRoomClicked = ::onRoomClicked,
        )

        DisposableEffect(Unit) {
            matrixClient.startSync()
            onDispose {
                matrixClient.stopSync()
            }
        }
    }
}

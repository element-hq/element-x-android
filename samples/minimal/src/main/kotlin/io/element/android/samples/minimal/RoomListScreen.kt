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
import io.element.android.features.leaveroom.impl.LeaveRoomPresenterImpl
import io.element.android.features.networkmonitor.impl.NetworkMonitorImpl
import io.element.android.features.roomlist.impl.DefaultInviteStateDataSource
import io.element.android.features.roomlist.impl.RoomListPresenter
import io.element.android.features.roomlist.impl.RoomListView
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.dateformatter.impl.DateFormatters
import io.element.android.libraries.dateformatter.impl.DefaultLastMessageTimestampFormatter
import io.element.android.libraries.dateformatter.impl.LocalDateTimeProvider
import io.element.android.libraries.designsystem.utils.SnackbarDispatcher
import io.element.android.libraries.eventformatter.impl.DefaultRoomLastMessageFormatter
import io.element.android.libraries.eventformatter.impl.ProfileChangeContentFormatter
import io.element.android.libraries.eventformatter.impl.RoomMembershipContentFormatter
import io.element.android.libraries.eventformatter.impl.StateContentFormatter
import io.element.android.libraries.eventformatter.impl.isme.DefaultIsMe
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.services.toolbox.impl.strings.AndroidStringProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import java.util.Locale

class RoomListScreen(
    context: Context,
    private val matrixClient: MatrixClient,
    private val coroutineDispatchers: CoroutineDispatchers = Singleton.coroutineDispatchers,
) {
    private val clock = Clock.System
    private val locale = Locale.getDefault()
    private val timeZone = TimeZone.currentSystemDefault()
    private val dateTimeProvider = LocalDateTimeProvider(clock, timeZone)
    private val dateFormatters = DateFormatters(locale, clock, timeZone)
    private val sessionVerificationService = matrixClient.sessionVerificationService()
    private val stringProvider = AndroidStringProvider(context.resources)
    private val isMe = DefaultIsMe(matrixClient)
    private val presenter = RoomListPresenter(
        client = matrixClient,
        lastMessageTimestampFormatter = DefaultLastMessageTimestampFormatter(dateTimeProvider, dateFormatters),
        roomLastMessageFormatter = DefaultRoomLastMessageFormatter(
            sp = stringProvider,
            isMe = isMe,
            roomMembershipContentFormatter = RoomMembershipContentFormatter(isMe, stringProvider),
            profileChangeContentFormatter = ProfileChangeContentFormatter(stringProvider),
            stateContentFormatter = StateContentFormatter(stringProvider),
        ),
        sessionVerificationService = sessionVerificationService,
        networkMonitor = NetworkMonitorImpl(context),
        snackbarDispatcher = SnackbarDispatcher(),
        inviteStateDataSource = DefaultInviteStateDataSource(matrixClient, DefaultSeenInvitesStore(context), coroutineDispatchers),
        leaveRoomPresenter = LeaveRoomPresenterImpl(matrixClient, RoomMembershipObserver(), coroutineDispatchers)
    )

    @Composable
    fun Content(modifier: Modifier = Modifier) {
        fun onRoomClicked(roomId: RoomId) {
            Singleton.appScope.launch {
                withContext(coroutineDispatchers.io) {
                    matrixClient.getRoom(roomId)!!.use { room ->
                        val timeline = room.timeline()

                        timeline.apply {
                            // TODO This doesn't work reliably as initialize is asynchronous, and the timeline can't be used until it's finished
                            initialize()
                            paginateBackwards(20, 50)
                            dispose()
                        }
                    }
                }
            }
        }

        val state = presenter.present()
        RoomListView(
            state = state,
            onRoomClicked = ::onRoomClicked,
            onSettingsClicked = {},
            onVerifyClicked = {},
            onCreateRoomClicked = {},
            onInvitesClicked = {},
            onRoomSettingsClicked = {},
            modifier = modifier,
        )

        DisposableEffect(Unit) {
            matrixClient.startSync()
            onDispose {
                matrixClient.stopSync()
            }
        }
    }
}

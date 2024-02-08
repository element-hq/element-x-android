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
import io.element.android.features.roomlist.impl.RoomListPresenter
import io.element.android.features.roomlist.impl.RoomListView
import io.element.android.features.roomlist.impl.datasource.DefaultInviteStateDataSource
import io.element.android.features.roomlist.impl.datasource.RoomListDataSource
import io.element.android.features.roomlist.impl.datasource.RoomListRoomSummaryFactory
import io.element.android.features.roomlist.impl.migration.MigrationScreenPresenter
import io.element.android.features.roomlist.impl.migration.SharedPrefsMigrationScreenStore
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.dateformatter.impl.DateFormatters
import io.element.android.libraries.dateformatter.impl.DefaultLastMessageTimestampFormatter
import io.element.android.libraries.dateformatter.impl.LocalDateTimeProvider
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.eventformatter.impl.DefaultRoomLastMessageFormatter
import io.element.android.libraries.eventformatter.impl.ProfileChangeContentFormatter
import io.element.android.libraries.eventformatter.impl.RoomMembershipContentFormatter
import io.element.android.libraries.eventformatter.impl.StateContentFormatter
import io.element.android.libraries.featureflag.impl.DefaultFeatureFlagService
import io.element.android.libraries.featureflag.impl.StaticFeatureFlagProvider
import io.element.android.libraries.indicator.impl.DefaultIndicatorService
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.services.toolbox.impl.strings.AndroidStringProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import timber.log.Timber
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
    private val encryptionService = matrixClient.encryptionService()
    private val stringProvider = AndroidStringProvider(context.resources)
    private val featureFlagService = DefaultFeatureFlagService(
        providers = setOf(StaticFeatureFlagProvider())
    )
    private val presenter = RoomListPresenter(
        client = matrixClient,
        sessionVerificationService = sessionVerificationService,
        networkMonitor = NetworkMonitorImpl(context, Singleton.appScope),
        snackbarDispatcher = SnackbarDispatcher(),
        inviteStateDataSource = DefaultInviteStateDataSource(matrixClient, DefaultSeenInvitesStore(context), coroutineDispatchers),
        leaveRoomPresenter = LeaveRoomPresenterImpl(matrixClient, RoomMembershipObserver(), coroutineDispatchers),
        roomListDataSource = RoomListDataSource(
            roomListService = matrixClient.roomListService,
            roomListRoomSummaryFactory = RoomListRoomSummaryFactory(
                lastMessageTimestampFormatter = DefaultLastMessageTimestampFormatter(
                    localDateTimeProvider = dateTimeProvider,
                    dateFormatters = dateFormatters
                ),
                roomLastMessageFormatter = DefaultRoomLastMessageFormatter(
                    sp = stringProvider,
                    roomMembershipContentFormatter = RoomMembershipContentFormatter(
                        matrixClient = matrixClient,
                        sp = stringProvider
                    ),
                    profileChangeContentFormatter = ProfileChangeContentFormatter(stringProvider),
                    stateContentFormatter = StateContentFormatter(stringProvider),
                ),
            ),
            coroutineDispatchers = coroutineDispatchers,
            notificationSettingsService = matrixClient.notificationSettingsService(),
            appScope = Singleton.appScope
        ),
        encryptionService = encryptionService,
        indicatorService = DefaultIndicatorService(
            sessionVerificationService = sessionVerificationService,
            encryptionService = encryptionService,
            featureFlagService = featureFlagService,
        ),
        featureFlagService = featureFlagService,
        migrationScreenPresenter = MigrationScreenPresenter(
            matrixClient = matrixClient,
            migrationScreenStore = SharedPrefsMigrationScreenStore(context.getSharedPreferences("migration", Context.MODE_PRIVATE))
        )
    )

    @Composable
    fun Content(modifier: Modifier = Modifier) {
        fun onRoomClicked(roomId: RoomId) {
            Singleton.appScope.launch {
                withContext(coroutineDispatchers.io) {
                    matrixClient.getRoom(roomId)!!.use { room ->
                        room.timeline.paginateBackwards(20, 50)
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
            onMenuActionClicked = {},
            modifier = modifier,
        )

        DisposableEffect(Unit) {
            Timber.w("Start sync!")
            runBlocking {
                matrixClient.syncService().startSync()
            }
            onDispose {
                Timber.w("Stop sync!")
                runBlocking {
                    matrixClient.syncService().stopSync()
                }
            }
        }
    }
}

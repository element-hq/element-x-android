/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.samples.minimal

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import io.element.android.features.invite.impl.response.AcceptDeclineInvitePresenter
import io.element.android.features.invite.impl.response.AcceptDeclineInviteView
import io.element.android.features.leaveroom.impl.LeaveRoomPresenter
import io.element.android.features.logout.impl.direct.DirectLogoutPresenter
import io.element.android.features.networkmonitor.impl.DefaultNetworkMonitor
import io.element.android.features.roomlist.impl.RoomListPresenter
import io.element.android.features.roomlist.impl.RoomListView
import io.element.android.features.roomlist.impl.datasource.RoomListDataSource
import io.element.android.features.roomlist.impl.datasource.RoomListRoomSummaryFactory
import io.element.android.features.roomlist.impl.filters.RoomListFiltersPresenter
import io.element.android.features.roomlist.impl.filters.selection.DefaultFilterSelectionStrategy
import io.element.android.features.roomlist.impl.search.RoomListSearchDataSource
import io.element.android.features.roomlist.impl.search.RoomListSearchPresenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.dateformatter.impl.DateFormatters
import io.element.android.libraries.dateformatter.impl.DefaultLastMessageTimestampFormatter
import io.element.android.libraries.dateformatter.impl.LocalDateTimeProvider
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.eventformatter.impl.DefaultRoomLastMessageFormatter
import io.element.android.libraries.eventformatter.impl.ProfileChangeContentFormatter
import io.element.android.libraries.eventformatter.impl.RoomMembershipContentFormatter
import io.element.android.libraries.eventformatter.impl.StateContentFormatter
import io.element.android.libraries.fullscreenintent.api.aFullScreenIntentPermissionsState
import io.element.android.libraries.indicator.impl.DefaultIndicatorService
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.impl.room.join.DefaultJoinRoom
import io.element.android.libraries.preferences.impl.store.DefaultSessionPreferencesStore
import io.element.android.libraries.push.test.notifications.FakeNotificationCleaner
import io.element.android.services.analytics.noop.NoopAnalyticsService
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
    private val featureFlagService = AlwaysEnabledFeatureFlagService()
    private val roomListRoomSummaryFactory = RoomListRoomSummaryFactory(
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
            permalinkParser = OnlyFallbackPermalinkParser(),
        ),
    )
    private val presenter = RoomListPresenter(
        client = matrixClient,
        networkMonitor = DefaultNetworkMonitor(context, Singleton.appScope),
        snackbarDispatcher = SnackbarDispatcher(),
        leaveRoomPresenter = LeaveRoomPresenter(matrixClient, RoomMembershipObserver(), coroutineDispatchers),
        roomListDataSource = RoomListDataSource(
            roomListService = matrixClient.roomListService,
            roomListRoomSummaryFactory = roomListRoomSummaryFactory,
            coroutineDispatchers = coroutineDispatchers,
            notificationSettingsService = matrixClient.notificationSettingsService(),
            appScope = Singleton.appScope
        ),
        indicatorService = DefaultIndicatorService(
            sessionVerificationService = sessionVerificationService,
            encryptionService = encryptionService,
        ),
        featureFlagService = featureFlagService,
        searchPresenter = RoomListSearchPresenter(
            RoomListSearchDataSource(
                roomListService = matrixClient.roomListService,
                roomSummaryFactory = roomListRoomSummaryFactory,
                coroutineDispatchers = coroutineDispatchers,
            ),
            featureFlagService = featureFlagService,
        ),
        sessionPreferencesStore = DefaultSessionPreferencesStore(
            context = context,
            sessionId = matrixClient.sessionId,
            sessionCoroutineScope = Singleton.appScope
        ),
        filtersPresenter = RoomListFiltersPresenter(
            roomListService = matrixClient.roomListService,
            filterSelectionStrategy = DefaultFilterSelectionStrategy(),
        ),
        acceptDeclineInvitePresenter = AcceptDeclineInvitePresenter(
            client = matrixClient,
            joinRoom = DefaultJoinRoom(matrixClient, NoopAnalyticsService()),
            notificationCleaner = FakeNotificationCleaner(),
        ),
        analyticsService = NoopAnalyticsService(),
        fullScreenIntentPermissionsPresenter = { aFullScreenIntentPermissionsState() },
        notificationCleaner = FakeNotificationCleaner(),
        logoutPresenter = DirectLogoutPresenter(matrixClient, encryptionService),
    )

    @Composable
    fun Content(modifier: Modifier = Modifier) {
        fun onRoomClick(roomId: RoomId) {
            Singleton.appScope.launch {
                withContext(coroutineDispatchers.io) {
                    matrixClient.getRoom(roomId)!!.use { room ->
                        room.liveTimeline.paginate(Timeline.PaginationDirection.BACKWARDS)
                    }
                }
            }
        }

        val state = presenter.present()
        RoomListView(
            state = state,
            onRoomClick = ::onRoomClick,
            onSettingsClick = {},
            onSetUpRecoveryClick = {},
            onConfirmRecoveryKeyClick = {},
            onCreateRoomClick = {},
            onRoomSettingsClick = {},
            onMenuActionClick = {},
            onRoomDirectorySearchClick = {},
            modifier = modifier,
            acceptDeclineInviteView = {
                AcceptDeclineInviteView(state = state.acceptDeclineInviteState, onAcceptInvite = {}, onDeclineInvite = {})
            },
            onMigrateToNativeSlidingSyncClick = {},
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

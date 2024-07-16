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
import io.element.android.features.invite.impl.response.AcceptDeclineInvitePresenter
import io.element.android.features.invite.impl.response.AcceptDeclineInviteView
import io.element.android.features.leaveroom.impl.DefaultLeaveRoomPresenter
import io.element.android.features.networkmonitor.impl.DefaultNetworkMonitor
import io.element.android.features.roomlist.impl.RoomListPresenter
import io.element.android.features.roomlist.impl.RoomListView
import io.element.android.features.roomlist.impl.datasource.RoomListDataSource
import io.element.android.features.roomlist.impl.datasource.RoomListRoomSummaryFactory
import io.element.android.features.roomlist.impl.filters.RoomListFiltersPresenter
import io.element.android.features.roomlist.impl.filters.selection.DefaultFilterSelectionStrategy
import io.element.android.features.roomlist.impl.migration.MigrationScreenPresenter
import io.element.android.features.roomlist.impl.migration.SharedPreferencesMigrationScreenStore
import io.element.android.features.roomlist.impl.search.RoomListSearchDataSource
import io.element.android.features.roomlist.impl.search.RoomListSearchPresenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.dateformatter.impl.DateFormatters
import io.element.android.libraries.dateformatter.impl.DefaultLastMessageTimestampFormatter
import io.element.android.libraries.dateformatter.impl.LocalDateTimeProvider
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.eventformatter.impl.DefaultRoomLastMessageFormatter
import io.element.android.libraries.eventformatter.impl.ProfileChangeContentFormatter
import io.element.android.libraries.eventformatter.impl.RoomMembershipContentFormatter
import io.element.android.libraries.eventformatter.impl.StateContentFormatter
import io.element.android.libraries.featureflag.impl.DefaultFeatureFlagService
import io.element.android.libraries.featureflag.impl.PreferencesFeatureFlagProvider
import io.element.android.libraries.fullscreenintent.api.FullScreenIntentPermissionsPresenter
import io.element.android.libraries.fullscreenintent.api.FullScreenIntentPermissionsState
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
    private val buildMeta = getBuildMeta(context)
    private val featureFlagService = DefaultFeatureFlagService(
        providers = setOf(
            PreferencesFeatureFlagProvider(context = context, buildMeta = buildMeta)
        ),
        buildMeta = buildMeta,
    )
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
        leaveRoomPresenter = DefaultLeaveRoomPresenter(matrixClient, RoomMembershipObserver(), coroutineDispatchers),
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
        migrationScreenPresenter = MigrationScreenPresenter(
            matrixClient = matrixClient,
            migrationScreenStore = SharedPreferencesMigrationScreenStore(context.getSharedPreferences("migration", Context.MODE_PRIVATE))
        ),
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
        fullScreenIntentPermissionsPresenter = object : FullScreenIntentPermissionsPresenter {
            @Composable
            override fun present(): FullScreenIntentPermissionsState {
                return FullScreenIntentPermissionsState(
                    permissionGranted = true,
                    shouldDisplayBanner = false,
                    dismissFullScreenIntentBanner = {},
                    openFullScreenIntentSettings = {}
                )
            }
        },
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
            onConfirmRecoveryKeyClick = {},
            onCreateRoomClick = {},
            onRoomSettingsClick = {},
            onMenuActionClick = {},
            onRoomDirectorySearchClick = {},
            modifier = modifier,
            acceptDeclineInviteView = {
                AcceptDeclineInviteView(state = state.acceptDeclineInviteState, onAcceptInvite = {}, onDeclineInvite = {})
            }
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

    private fun getBuildMeta(context: Context): BuildMeta {
        val buildType = BuildType.valueOf(BuildConfig.BUILD_TYPE.uppercase())
        val name = context.getString(R.string.app_name)
        return BuildMeta(
            isDebuggable = BuildConfig.DEBUG,
            buildType = buildType,
            applicationName = name,
            productionApplicationName = name,
            desktopApplicationName = name,
            applicationId = BuildConfig.APPLICATION_ID,
            lowPrivacyLoggingEnabled = false,
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE.toLong(),
            gitRevision = "",
            gitBranchName = "",
            flavorDescription = "",
            flavorShortDescription = "",
            isEnterpriseBuild = false,
        )
    }
}

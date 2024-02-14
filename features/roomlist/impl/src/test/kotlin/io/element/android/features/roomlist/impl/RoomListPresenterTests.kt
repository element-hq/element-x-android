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

package io.element.android.features.roomlist.impl

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.leaveroom.api.LeaveRoomEvent
import io.element.android.features.leaveroom.api.LeaveRoomPresenter
import io.element.android.features.leaveroom.fake.FakeLeaveRoomPresenter
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.test.FakeNetworkMonitor
import io.element.android.features.preferences.api.store.SessionPreferencesStore
import io.element.android.features.roomlist.impl.datasource.FakeInviteDataSource
import io.element.android.features.roomlist.impl.datasource.InviteStateDataSource
import io.element.android.features.roomlist.impl.datasource.RoomListDataSource
import io.element.android.features.roomlist.impl.datasource.RoomListRoomSummaryFactory
import io.element.android.features.roomlist.impl.migration.InMemoryMigrationScreenStore
import io.element.android.features.roomlist.impl.migration.MigrationScreenPresenter
import io.element.android.features.roomlist.impl.model.createRoomListRoomSummary
import io.element.android.libraries.dateformatter.api.LastMessageTimestampFormatter
import io.element.android.libraries.dateformatter.test.A_FORMATTED_DATE
import io.element.android.libraries.dateformatter.test.FakeLastMessageTimestampFormatter
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.eventformatter.api.RoomLastMessageFormatter
import io.element.android.libraries.eventformatter.test.FakeRoomLastMessageFormatter
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.featureflag.test.InMemorySessionPreferencesStore
import io.element.android.libraries.indicator.impl.DefaultIndicatorService
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.encryption.BackupState
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.matrix.test.notificationsettings.FakeNotificationSettingsService
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomSummaryFilled
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class RoomListPresenterTests {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - should start with no user and then load user with success`() = runTest {
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(coroutineScope = scope)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.matrixUser).isNull()
            val withUserState = awaitItem()
            assertThat(withUserState.matrixUser).isNotNull()
            assertThat(withUserState.matrixUser!!.userId).isEqualTo(A_USER_ID)
            assertThat(withUserState.matrixUser!!.displayName).isEqualTo(A_USER_NAME)
            assertThat(withUserState.matrixUser!!.avatarUrl).isEqualTo(AN_AVATAR_URL)
            assertThat(withUserState.showAvatarIndicator).isFalse()
            scope.cancel()
        }
    }

    @Test
    fun `present - show avatar indicator`() = runTest {
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val encryptionService = FakeEncryptionService()
        val sessionVerificationService = FakeSessionVerificationService()
        val presenter = createRoomListPresenter(
            encryptionService = encryptionService,
            sessionVerificationService = sessionVerificationService,
            coroutineScope = scope
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.showAvatarIndicator).isFalse()
            sessionVerificationService.givenCanVerifySession(false)
            assertThat(awaitItem().showAvatarIndicator).isFalse()
            encryptionService.emitBackupState(BackupState.UNKNOWN)
            val finalState = awaitItem()
            assertThat(finalState.showAvatarIndicator).isTrue()
            scope.cancel()
        }
    }

    @Test
    fun `present - should start with no user and then load user with error`() = runTest {
        val matrixClient = FakeMatrixClient(
            userDisplayName = Result.failure(AN_EXCEPTION),
            userAvatarUrl = Result.failure(AN_EXCEPTION),
        )
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(client = matrixClient, coroutineScope = scope)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.matrixUser).isNull()
            val withUserState = awaitItem()
            assertThat(withUserState.matrixUser).isNotNull()
            scope.cancel()
        }
    }

    @Test
    fun `present - should filter room with success`() = runTest {
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(coroutineScope = scope)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val withUserState = awaitItem()
            assertThat(withUserState.filter).isEqualTo("")
            withUserState.eventSink.invoke(RoomListEvents.UpdateFilter("t"))
            val withFilterState = awaitItem()
            assertThat(withFilterState.filter).isEqualTo("t")
            cancelAndIgnoreRemainingEvents()
            scope.cancel()
        }
    }

    @Test
    fun `present - load 1 room with success`() = runTest {
        val roomListService = FakeRoomListService()
        val matrixClient = FakeMatrixClient(
            roomListService = roomListService
        )
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(client = matrixClient, coroutineScope = scope)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = consumeItemsUntilPredicate { state -> state.roomList.dataOrNull()?.size == 16 }.last()
            // Room list is loaded with 16 placeholders
            val initialItems = initialState.roomList.dataOrNull().orEmpty()
            assertThat(initialItems.size).isEqualTo(16)
            assertThat(initialItems.all { it.isPlaceholder }).isTrue()
            roomListService.postAllRooms(
                listOf(
                    aRoomSummaryFilled(
                        numUnreadMentions = 1,
                        numUnreadMessages = 2,
                    )
                )
            )
            val withRoomState = consumeItemsUntilPredicate { state -> state.roomList.dataOrNull()?.size == 1 }.last()
            val withRoomStateItems = withRoomState.roomList.dataOrNull().orEmpty()
            assertThat(withRoomStateItems.size).isEqualTo(1)
            assertThat(withRoomStateItems.first()).isEqualTo(
                createRoomListRoomSummary(
                    numberOfUnreadMentions = 1,
                    numberOfUnreadMessages = 2,
                )
            )
            scope.cancel()
        }
    }

    @Test
    fun `present - load 1 room with success and filter rooms`() = runTest {
        val roomListService = FakeRoomListService()
        val matrixClient = FakeMatrixClient(
            roomListService = roomListService
        )
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(client = matrixClient, coroutineScope = scope)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            roomListService.postAllRooms(
                listOf(
                    aRoomSummaryFilled(
                        numUnreadMentions = 1,
                        numUnreadMessages = 2,
                    )
                )
            )
            skipItems(3)
            val loadedState = awaitItem()
            // Test filtering with result
            assertThat(loadedState.roomList.dataOrNull().orEmpty().size).isEqualTo(1)
            loadedState.eventSink.invoke(RoomListEvents.UpdateFilter(A_ROOM_NAME.substring(0, 3)))
            skipItems(1)
            val withFilteredRoomState = awaitItem()
            assertThat(withFilteredRoomState.filteredRoomList.size).isEqualTo(1)
            assertThat(withFilteredRoomState.filter).isEqualTo(A_ROOM_NAME.substring(0, 3))
            assertThat(withFilteredRoomState.filteredRoomList.size).isEqualTo(1)
            assertThat(withFilteredRoomState.filteredRoomList.first()).isEqualTo(
                createRoomListRoomSummary(
                    numberOfUnreadMentions = 1,
                    numberOfUnreadMessages = 2,
                )
            )
            // Test filtering without result
            withFilteredRoomState.eventSink.invoke(RoomListEvents.UpdateFilter("tada"))
            skipItems(1)
            val withNotFilteredRoomState = awaitItem()
            assertThat(withNotFilteredRoomState.filter).isEqualTo("tada")
            assertThat(withNotFilteredRoomState.filteredRoomList).isEmpty()
            scope.cancel()
        }
    }

    @Test
    fun `present - update visible range`() = runTest {
        val roomListService = FakeRoomListService()
        val matrixClient = FakeMatrixClient(
            roomListService = roomListService
        )
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(client = matrixClient, coroutineScope = scope)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            roomListService.postAllRooms(listOf(aRoomSummaryFilled()))
            val loadedState = awaitItem()
            // check initial value
            assertThat(roomListService.latestSlidingSyncRange).isNull()
            // Test empty range
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(1, 0)))
            assertThat(roomListService.latestSlidingSyncRange).isNull()
            // Update visible range and check that range is transmitted to the SDK after computation
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(0, 0)))
            assertThat(roomListService.latestSlidingSyncRange)
                .isEqualTo(IntRange(0, 20))
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(0, 1)))
            assertThat(roomListService.latestSlidingSyncRange)
                .isEqualTo(IntRange(0, 21))
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(19, 29)))
            assertThat(roomListService.latestSlidingSyncRange)
                .isEqualTo(IntRange(0, 49))
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(49, 59)))
            assertThat(roomListService.latestSlidingSyncRange)
                .isEqualTo(IntRange(29, 79))
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(149, 159)))
            assertThat(roomListService.latestSlidingSyncRange)
                .isEqualTo(IntRange(129, 179))
            loadedState.eventSink.invoke(RoomListEvents.UpdateVisibleRange(IntRange(149, 259)))
            assertThat(roomListService.latestSlidingSyncRange)
                .isEqualTo(IntRange(129, 279))
            cancelAndIgnoreRemainingEvents()
            scope.cancel()
        }
    }

    @Test
    fun `present - handle DismissRequestVerificationPrompt`() = runTest {
        val roomListService = FakeRoomListService()
        val matrixClient = FakeMatrixClient(
            roomListService = roomListService,
        )
        val scope = CoroutineScope(context = coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(
            client = matrixClient,
            sessionVerificationService = FakeSessionVerificationService().apply {
                givenIsReady(true)
                givenVerifiedStatus(SessionVerifiedStatus.NotVerified)
            },
            coroutineScope = scope,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val eventSink = awaitItem().eventSink
            assertThat(awaitItem().displayVerificationPrompt).isTrue()

            eventSink(RoomListEvents.DismissRequestVerificationPrompt)
            assertThat(awaitItem().displayVerificationPrompt).isFalse()
            scope.cancel()
        }
    }

    @Test
    fun `present - sets invite state`() = runTest {
        val inviteStateFlow = MutableStateFlow(InvitesState.NoInvites)
        val inviteStateDataSource = FakeInviteDataSource(inviteStateFlow)
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(inviteStateDataSource = inviteStateDataSource, coroutineScope = scope)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            assertThat(awaitItem().invitesState).isEqualTo(InvitesState.NoInvites)

            inviteStateFlow.value = InvitesState.SeenInvites
            assertThat(awaitItem().invitesState).isEqualTo(InvitesState.SeenInvites)

            inviteStateFlow.value = InvitesState.NewInvites
            assertThat(awaitItem().invitesState).isEqualTo(InvitesState.NewInvites)

            inviteStateFlow.value = InvitesState.NoInvites
            assertThat(awaitItem().invitesState).isEqualTo(InvitesState.NoInvites)
            scope.cancel()
        }
    }

    @Test
    fun `present - show context menu`() = runTest {
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(coroutineScope = scope)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)

            val initialState = awaitItem()
            val summary = createRoomListRoomSummary()
            initialState.eventSink(RoomListEvents.ShowContextMenu(summary))

            val shownState = awaitItem()
            assertThat(shownState.contextMenu).isEqualTo(
                RoomListState.ContextMenu.Shown(
                    roomId = summary.roomId,
                    roomName = summary.name,
                    isDm = false,
                    markAsUnreadFeatureFlagEnabled = true,
                    hasNewContent = false,
                )
            )
            scope.cancel()
        }
    }

    @Test
    fun `present - hide context menu`() = runTest {
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(coroutineScope = scope)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)

            val initialState = awaitItem()
            val summary = createRoomListRoomSummary()
            initialState.eventSink(RoomListEvents.ShowContextMenu(summary))

            val shownState = awaitItem()
            assertThat(shownState.contextMenu).isEqualTo(
                RoomListState.ContextMenu.Shown(
                    roomId = summary.roomId,
                    roomName = summary.name,
                    isDm = false,
                    markAsUnreadFeatureFlagEnabled = true,
                    hasNewContent = false,
                )
            )
            shownState.eventSink(RoomListEvents.HideContextMenu)

            val hiddenState = awaitItem()
            assertThat(hiddenState.contextMenu).isEqualTo(RoomListState.ContextMenu.Hidden)
            scope.cancel()
        }
    }

    @Test
    fun `present - leave room calls into leave room presenter`() = runTest {
        val leaveRoomPresenter = FakeLeaveRoomPresenter()
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(leaveRoomPresenter = leaveRoomPresenter, coroutineScope = scope)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomListEvents.LeaveRoom(A_ROOM_ID))
            assertThat(leaveRoomPresenter.events).containsExactly(LeaveRoomEvent.ShowConfirmation(A_ROOM_ID))
            cancelAndIgnoreRemainingEvents()
            scope.cancel()
        }
    }

    @Test
    fun `present - change in notification settings updates the summary for decorations`() = runTest {
        val userDefinedMode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY
        val notificationSettingsService = FakeNotificationSettingsService()
        val roomListService = FakeRoomListService()
        roomListService.postAllRooms(listOf(aRoomSummaryFilled(notificationMode = userDefinedMode)))
        val matrixClient = FakeMatrixClient(
            roomListService = roomListService,
            notificationSettingsService = notificationSettingsService
        )
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(client = matrixClient, coroutineScope = scope)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            notificationSettingsService.setRoomNotificationMode(A_ROOM_ID, userDefinedMode)

            val updatedState = consumeItemsUntilPredicate { state ->
                state.roomList.dataOrNull().orEmpty().any { it.id == A_ROOM_ID.value && it.userDefinedNotificationMode == userDefinedMode }
            }.last()

            val room = updatedState.roomList.dataOrNull()?.find { it.id == A_ROOM_ID.value }
            assertThat(room?.userDefinedNotificationMode).isEqualTo(userDefinedMode)
            cancelAndIgnoreRemainingEvents()
            scope.cancel()
        }
    }

    @Test
    fun `present - change in migration presenter state modifies isMigrating`() = runTest {
        val client = FakeMatrixClient(sessionId = A_SESSION_ID)
        val migrationStore = InMemoryMigrationScreenStore()
        val migrationScreenPresenter = MigrationScreenPresenter(client, migrationStore)
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(
            client = client,
            coroutineScope = scope,
            migrationScreenPresenter = migrationScreenPresenter,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            // The migration screen is shown if the migration screen has not been shown before
            assertThat(initialState.displayMigrationStatus).isTrue()
            skipItems(2)

            // Set migration as done and set the room list service as running to trigger a refresh of the presenter value
            (client.roomListService as FakeRoomListService).postState(RoomListService.State.Running)
            migrationStore.setMigrationScreenShown(A_SESSION_ID)

            // The migration screen is not shown anymore
            assertThat(awaitItem().displayMigrationStatus).isFalse()
            cancelAndIgnoreRemainingEvents()
            scope.cancel()
        }
    }

    @Test
    fun `present - check that the room is marked as read with correct RR and as unread`() = runTest {
        val room = FakeMatrixRoom()
        val sessionPreferencesStore = InMemorySessionPreferencesStore()
        val matrixClient = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, room)
        }
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(
            client = matrixClient,
            coroutineScope = scope,
            sessionPreferencesStore = sessionPreferencesStore,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(room.markAsReadCalls).isEmpty()
            assertThat(room.setUnreadFlagCalls).isEmpty()
            initialState.eventSink.invoke(RoomListEvents.MarkAsRead(A_ROOM_ID))
            assertThat(room.markAsReadCalls).isEqualTo(listOf(ReceiptType.READ))
            assertThat(room.setUnreadFlagCalls).isEqualTo(listOf(false))
            initialState.eventSink.invoke(RoomListEvents.MarkAsUnread(A_ROOM_ID))
            assertThat(room.markAsReadCalls).isEqualTo(listOf(ReceiptType.READ))
            assertThat(room.setUnreadFlagCalls).isEqualTo(listOf(false, true))
            // Test again with private read receipts
            sessionPreferencesStore.setSendPublicReadReceipts(false)
            initialState.eventSink.invoke(RoomListEvents.MarkAsRead(A_ROOM_ID))
            assertThat(room.markAsReadCalls).isEqualTo(listOf(ReceiptType.READ, ReceiptType.READ_PRIVATE))
            assertThat(room.setUnreadFlagCalls).isEqualTo(listOf(false, true, false))
            cancelAndIgnoreRemainingEvents()
            scope.cancel()
        }
    }

    private fun TestScope.createRoomListPresenter(
        client: MatrixClient = FakeMatrixClient(),
        sessionVerificationService: SessionVerificationService = FakeSessionVerificationService(),
        networkMonitor: NetworkMonitor = FakeNetworkMonitor(),
        snackbarDispatcher: SnackbarDispatcher = SnackbarDispatcher(),
        inviteStateDataSource: InviteStateDataSource = FakeInviteDataSource(),
        leaveRoomPresenter: LeaveRoomPresenter = FakeLeaveRoomPresenter(),
        lastMessageTimestampFormatter: LastMessageTimestampFormatter = FakeLastMessageTimestampFormatter().apply {
            givenFormat(A_FORMATTED_DATE)
        },
        roomLastMessageFormatter: RoomLastMessageFormatter = FakeRoomLastMessageFormatter(),
        encryptionService: EncryptionService = FakeEncryptionService(),
        sessionPreferencesStore: SessionPreferencesStore = InMemorySessionPreferencesStore(),
        coroutineScope: CoroutineScope,
        migrationScreenPresenter: MigrationScreenPresenter = MigrationScreenPresenter(
            matrixClient = client,
            migrationScreenStore = InMemoryMigrationScreenStore(),
        )
    ) = RoomListPresenter(
        client = client,
        sessionVerificationService = sessionVerificationService,
        networkMonitor = networkMonitor,
        snackbarDispatcher = snackbarDispatcher,
        inviteStateDataSource = inviteStateDataSource,
        leaveRoomPresenter = leaveRoomPresenter,
        roomListDataSource = RoomListDataSource(
            roomListService = client.roomListService,
            roomListRoomSummaryFactory = RoomListRoomSummaryFactory(
                lastMessageTimestampFormatter = lastMessageTimestampFormatter,
                roomLastMessageFormatter = roomLastMessageFormatter,
            ),
            coroutineDispatchers = testCoroutineDispatchers(),
            notificationSettingsService = client.notificationSettingsService(),
            appScope = coroutineScope
        ),
        encryptionService = encryptionService,
        featureFlagService = FakeFeatureFlagService(mapOf(FeatureFlags.SecureStorage.key to true)),
        indicatorService = DefaultIndicatorService(
            sessionVerificationService = sessionVerificationService,
            encryptionService = encryptionService,
            featureFlagService = FakeFeatureFlagService(mapOf(FeatureFlags.SecureStorage.key to true)),
        ),
        migrationScreenPresenter = migrationScreenPresenter,
        sessionPreferencesStore = sessionPreferencesStore,
    )
}

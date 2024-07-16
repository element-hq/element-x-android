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
import im.vector.app.features.analytics.plan.Interaction
import io.element.android.features.invite.api.response.AcceptDeclineInviteEvents
import io.element.android.features.invite.api.response.AcceptDeclineInviteState
import io.element.android.features.invite.api.response.anAcceptDeclineInviteState
import io.element.android.features.leaveroom.api.LeaveRoomEvent
import io.element.android.features.leaveroom.api.LeaveRoomPresenter
import io.element.android.features.leaveroom.fake.FakeLeaveRoomPresenter
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.test.FakeNetworkMonitor
import io.element.android.features.roomlist.impl.datasource.RoomListDataSource
import io.element.android.features.roomlist.impl.datasource.RoomListRoomSummaryFactory
import io.element.android.features.roomlist.impl.filters.RoomListFiltersState
import io.element.android.features.roomlist.impl.filters.aRoomListFiltersState
import io.element.android.features.roomlist.impl.migration.MigrationScreenState
import io.element.android.features.roomlist.impl.model.createRoomListRoomSummary
import io.element.android.features.roomlist.impl.search.RoomListSearchEvents
import io.element.android.features.roomlist.impl.search.RoomListSearchState
import io.element.android.features.roomlist.impl.search.aRoomListSearchState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.dateformatter.api.LastMessageTimestampFormatter
import io.element.android.libraries.dateformatter.test.A_FORMATTED_DATE
import io.element.android.libraries.dateformatter.test.FakeLastMessageTimestampFormatter
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.eventformatter.api.RoomLastMessageFormatter
import io.element.android.libraries.eventformatter.test.FakeRoomLastMessageFormatter
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.fullscreenintent.test.FakeFullScreenIntentPermissionsPresenter
import io.element.android.libraries.indicator.impl.DefaultIndicatorService
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.encryption.BackupState
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.sync.SyncState
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_ID_3
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.matrix.test.notificationsettings.FakeNotificationSettingsService
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.matrix.test.room.aRoomSummary
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.libraries.matrix.test.sync.FakeSyncService
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
import io.element.android.libraries.push.api.notifications.NotificationCleaner
import io.element.android.libraries.push.test.notifications.FakeNotificationCleaner
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.MutablePresenter
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class RoomListPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - should start with no user and then load user with success`() = runTest {
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val matrixClient = FakeMatrixClient(
            userDisplayName = null,
            userAvatarUrl = null,
        )
        matrixClient.givenGetProfileResult(matrixClient.sessionId, Result.success(MatrixUser(matrixClient.sessionId, A_USER_NAME, AN_AVATAR_URL)))
        val presenter = createRoomListPresenter(
            client = matrixClient,
            coroutineScope = scope,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.matrixUser).isEqualTo(MatrixUser(A_USER_ID))
            val withUserState = awaitItem()
            assertThat(withUserState.matrixUser.userId).isEqualTo(A_USER_ID)
            assertThat(withUserState.matrixUser.displayName).isEqualTo(A_USER_NAME)
            assertThat(withUserState.matrixUser.avatarUrl).isEqualTo(AN_AVATAR_URL)
            assertThat(withUserState.showAvatarIndicator).isTrue()
            scope.cancel()
        }
    }

    @Test
    fun `present - show avatar indicator`() = runTest {
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val encryptionService = FakeEncryptionService()
        val sessionVerificationService = FakeSessionVerificationService()
        val matrixClient = FakeMatrixClient(
            encryptionService = encryptionService,
            sessionVerificationService = sessionVerificationService,
        )
        val presenter = createRoomListPresenter(
            client = matrixClient,
            coroutineScope = scope
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.showAvatarIndicator).isTrue()
            sessionVerificationService.givenNeedsSessionVerification(false)
            encryptionService.emitBackupState(BackupState.ENABLED)
            val finalState = awaitItem()
            assertThat(finalState.showAvatarIndicator).isFalse()
            scope.cancel()
        }
    }

    @Test
    fun `present - should start with no user and then load user with error`() = runTest {
        val matrixClient = FakeMatrixClient(
            userDisplayName = null,
            userAvatarUrl = null,
        )
        matrixClient.givenGetProfileResult(matrixClient.sessionId, Result.failure(AN_EXCEPTION))
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(client = matrixClient, coroutineScope = scope)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.matrixUser).isEqualTo(MatrixUser(matrixClient.sessionId))
            // No new state is coming
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
            val initialState = consumeItemsUntilPredicate { state -> state.contentState is RoomListContentState.Skeleton }.last()
            assertThat(initialState.contentState).isInstanceOf(RoomListContentState.Skeleton::class.java)
            roomListService.postAllRoomsLoadingState(RoomList.LoadingState.Loaded(1))
            roomListService.postAllRooms(
                listOf(
                    aRoomSummary(
                        numUnreadMentions = 1,
                        numUnreadMessages = 2,
                    )
                )
            )
            val withRoomsState =
                consumeItemsUntilPredicate { state -> state.contentState is RoomListContentState.Rooms && state.contentAsRooms().summaries.isNotEmpty() }.last()
            assertThat(withRoomsState.contentAsRooms().summaries).hasSize(1)
            assertThat(withRoomsState.contentAsRooms().summaries.first()).isEqualTo(
                createRoomListRoomSummary(
                    numberOfUnreadMentions = 1,
                    numberOfUnreadMessages = 2,
                )
            )
            cancelAndIgnoreRemainingEvents()
            scope.cancel()
        }
    }

    @Test
    fun `present - handle DismissRequestVerificationPrompt`() = runTest {
        val scope = CoroutineScope(context = coroutineContext + SupervisorJob())
        val roomListService = FakeRoomListService().apply {
            postAllRoomsLoadingState(RoomList.LoadingState.Loaded(1))
        }
        val encryptionService = FakeEncryptionService().apply {
            emitRecoveryState(RecoveryState.INCOMPLETE)
        }
        val syncService = FakeSyncService(initialState = SyncState.Running)
        val presenter = createRoomListPresenter(
            client = FakeMatrixClient(roomListService = roomListService, encryptionService = encryptionService, syncService = syncService),
            coroutineScope = scope,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val eventWithContentAsRooms = consumeItemsUntilPredicate {
                it.contentState is RoomListContentState.Rooms
            }.last()
            val eventSink = eventWithContentAsRooms.eventSink
            assertThat(eventWithContentAsRooms.contentAsRooms().securityBannerState).isEqualTo(SecurityBannerState.RecoveryKeyConfirmation)
            eventSink(RoomListEvents.DismissRequestVerificationPrompt)
            assertThat(awaitItem().contentAsRooms().securityBannerState).isEqualTo(SecurityBannerState.None)
            scope.cancel()
        }
    }

    @Test
    fun `present - handle DismissRecoveryKeyPrompt`() = runTest {
        val encryptionService = FakeEncryptionService()
        val roomListService = FakeRoomListService().apply {
            postAllRoomsLoadingState(RoomList.LoadingState.Loaded(1))
        }
        val matrixClient = FakeMatrixClient(
            roomListService = roomListService,
            encryptionService = encryptionService,
            sessionVerificationService = FakeSessionVerificationService().apply {
                givenNeedsSessionVerification(false)
            },
            syncService = FakeSyncService(initialState = SyncState.Running)
        )
        val scope = CoroutineScope(context = coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(
            client = matrixClient,
            coroutineScope = scope,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = consumeItemsUntilPredicate {
                it.contentState is RoomListContentState.Rooms
            }.last()
            assertThat(initialState.contentAsRooms().securityBannerState).isEqualTo(SecurityBannerState.None)
            encryptionService.emitRecoveryState(RecoveryState.INCOMPLETE)
            val nextState = awaitItem()
            assertThat(nextState.contentAsRooms().securityBannerState).isEqualTo(SecurityBannerState.RecoveryKeyConfirmation)
            nextState.eventSink(RoomListEvents.DismissRecoveryKeyPrompt)
            val finalState = awaitItem()
            assertThat(finalState.contentAsRooms().securityBannerState).isEqualTo(SecurityBannerState.None)
            scope.cancel()
        }
    }

    @Test
    fun `present - show context menu`() = runTest {
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val room = FakeMatrixRoom()
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, room)
        }
        val presenter = createRoomListPresenter(client = client, coroutineScope = scope)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val summary = createRoomListRoomSummary()
            initialState.eventSink(RoomListEvents.ShowContextMenu(summary))

            awaitItem().also { state ->
                assertThat(state.contextMenu)
                    .isEqualTo(
                        RoomListState.ContextMenu.Shown(
                            roomId = summary.roomId,
                            roomName = summary.name,
                            isDm = false,
                            isFavorite = false,
                            markAsUnreadFeatureFlagEnabled = true,
                            hasNewContent = false,
                        )
                    )
            }

            room.givenRoomInfo(
                aRoomInfo(isFavorite = true)
            )
            awaitItem().also { state ->
                assertThat(state.contextMenu)
                    .isEqualTo(
                        RoomListState.ContextMenu.Shown(
                            roomId = summary.roomId,
                            roomName = summary.name,
                            isDm = false,
                            isFavorite = true,
                            markAsUnreadFeatureFlagEnabled = true,
                            hasNewContent = false,
                        )
                    )
            }
            scope.cancel()
        }
    }

    @Test
    fun `present - hide context menu`() = runTest {
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val room = FakeMatrixRoom()
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, room)
        }
        val presenter = createRoomListPresenter(client = client, coroutineScope = scope)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val summary = createRoomListRoomSummary()
            initialState.eventSink(RoomListEvents.ShowContextMenu(summary))

            val shownState = awaitItem()
            assertThat(shownState.contextMenu)
                .isEqualTo(
                    RoomListState.ContextMenu.Shown(
                        roomId = summary.roomId,
                        roomName = summary.name,
                        isDm = false,
                        isFavorite = false,
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
    fun `present - toggle search menu`() = runTest {
        val eventRecorder = EventsRecorder<RoomListSearchEvents>()
        val searchPresenter: Presenter<RoomListSearchState> = Presenter {
            aRoomListSearchState(
                eventSink = eventRecorder
            )
        }
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(
            coroutineScope = scope,
            searchPresenter = searchPresenter,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            eventRecorder.assertEmpty()
            initialState.eventSink(RoomListEvents.ToggleSearchResults)
            eventRecorder.assertSingle(
                RoomListSearchEvents.ToggleSearchVisibility
            )
            initialState.eventSink(RoomListEvents.ToggleSearchResults)
            eventRecorder.assertList(
                listOf(
                    RoomListSearchEvents.ToggleSearchVisibility,
                    RoomListSearchEvents.ToggleSearchVisibility
                )
            )
            scope.cancel()
        }
    }

    @Test
    fun `present - change in notification settings updates the summary for decorations`() = runTest {
        val userDefinedMode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY
        val notificationSettingsService = FakeNotificationSettingsService()
        val roomListService = FakeRoomListService()
        roomListService.postAllRoomsLoadingState(RoomList.LoadingState.Loaded(1))
        roomListService.postAllRooms(listOf(aRoomSummary(notificationMode = userDefinedMode)))
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
                (state.contentState as? RoomListContentState.Rooms)?.summaries.orEmpty().any { summary ->
                    summary.id == A_ROOM_ID.value && summary.userDefinedNotificationMode == userDefinedMode
                }
            }.last()

            val room = updatedState.contentAsRooms().summaries.find { it.id == A_ROOM_ID.value }
            assertThat(room?.userDefinedNotificationMode).isEqualTo(userDefinedMode)
            cancelAndIgnoreRemainingEvents()
            scope.cancel()
        }
    }

    @Test
    fun `present - when set is favorite event is emitted, then the action is called`() = runTest {
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val room = FakeMatrixRoom()
        val analyticsService = FakeAnalyticsService()
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, room)
        }
        val presenter = createRoomListPresenter(client = client, coroutineScope = scope, analyticsService = analyticsService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomListEvents.SetRoomIsFavorite(A_ROOM_ID, true))
            assertThat(room.setIsFavoriteCalls).isEqualTo(listOf(true))
            initialState.eventSink(RoomListEvents.SetRoomIsFavorite(A_ROOM_ID, false))
            assertThat(room.setIsFavoriteCalls).isEqualTo(listOf(true, false))
            assertThat(analyticsService.capturedEvents).containsExactly(
                Interaction(name = Interaction.Name.MobileRoomListRoomContextMenuFavouriteToggle),
                Interaction(name = Interaction.Name.MobileRoomListRoomContextMenuFavouriteToggle)
            )
            cancelAndIgnoreRemainingEvents()
            scope.cancel()
        }
    }

    @Test
    fun `present - change in migration presenter state modifies contentState`() = runTest {
        val migrationScreenPresenter = MutablePresenter(MigrationScreenState(true))
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(
            coroutineScope = scope,
            migrationScreenPresenter = migrationScreenPresenter,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            // The migration screen is shown if the migration screen has not been shown before
            assertThat(initialState.contentState).isInstanceOf(RoomListContentState.Migration::class.java)
            // Set migration as done and set the room list service as running to trigger a refresh of the presenter value
            migrationScreenPresenter.updateState(MigrationScreenState(false))
            // The migration screen is not shown anymore
            assertThat(awaitItem().contentState).isInstanceOf(RoomListContentState.Skeleton::class.java)
            scope.cancel()
        }
    }

    @Test
    fun `present - when room service returns no room, then contentState is Empty `() = runTest {
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val roomListService = FakeRoomListService()
        roomListService.postAllRoomsLoadingState(RoomList.LoadingState.Loaded(0))
        val matrixClient = FakeMatrixClient(
            roomListService = roomListService,
        )
        val presenter = createRoomListPresenter(
            client = matrixClient,
            coroutineScope = scope,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().contentState).isInstanceOf(RoomListContentState.Empty::class.java)
            scope.cancel()
        }
    }

    @Test
    fun `present - check that the room is marked as read with correct RR and as unread`() = runTest {
        val room = FakeMatrixRoom()
        val room2 = FakeMatrixRoom(roomId = A_ROOM_ID_2)
        val room3 = FakeMatrixRoom(roomId = A_ROOM_ID_3)
        val allRooms = setOf(room, room2, room3)
        val sessionPreferencesStore = InMemorySessionPreferencesStore()
        val matrixClient = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, room)
            givenGetRoomResult(A_ROOM_ID_2, room2)
            givenGetRoomResult(A_ROOM_ID_3, room3)
        }
        val analyticsService = FakeAnalyticsService()
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val clearMessagesForRoomLambda = lambdaRecorder<SessionId, RoomId, Unit> { _, _ -> }
        val notificationCleaner = FakeNotificationCleaner(
            clearMessagesForRoomLambda = clearMessagesForRoomLambda,
        )
        val presenter = createRoomListPresenter(
            client = matrixClient,
            coroutineScope = scope,
            sessionPreferencesStore = sessionPreferencesStore,
            analyticsService = analyticsService,
            notificationCleaner = notificationCleaner,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            allRooms.forEach {
                assertThat(it.markAsReadCalls).isEmpty()
                assertThat(it.setUnreadFlagCalls).isEmpty()
            }
            initialState.eventSink.invoke(RoomListEvents.MarkAsRead(A_ROOM_ID))
            assertThat(room.markAsReadCalls).isEqualTo(listOf(ReceiptType.READ))
            assertThat(room.setUnreadFlagCalls).isEqualTo(listOf(false))
            clearMessagesForRoomLambda.assertions().isCalledOnce()
                .with(value(A_SESSION_ID), value(A_ROOM_ID))
            initialState.eventSink.invoke(RoomListEvents.MarkAsUnread(A_ROOM_ID_2))
            assertThat(room2.markAsReadCalls).isEqualTo(emptyList<ReceiptType>())
            assertThat(room2.setUnreadFlagCalls).isEqualTo(listOf(true))
            // Test again with private read receipts
            sessionPreferencesStore.setSendPublicReadReceipts(false)
            initialState.eventSink.invoke(RoomListEvents.MarkAsRead(A_ROOM_ID_3))
            assertThat(room3.markAsReadCalls).isEqualTo(listOf(ReceiptType.READ_PRIVATE))
            assertThat(room3.setUnreadFlagCalls).isEqualTo(listOf(false))
            clearMessagesForRoomLambda.assertions().isCalledExactly(2)
                .withSequence(
                    listOf(value(A_SESSION_ID), value(A_ROOM_ID)),
                    listOf(value(A_SESSION_ID), value(A_ROOM_ID_3)),
                )
            assertThat(analyticsService.capturedEvents).containsExactly(
                Interaction(name = Interaction.Name.MobileRoomListRoomContextMenuUnreadToggle),
                Interaction(name = Interaction.Name.MobileRoomListRoomContextMenuUnreadToggle),
                Interaction(name = Interaction.Name.MobileRoomListRoomContextMenuUnreadToggle),
            )
            cancelAndIgnoreRemainingEvents()
            scope.cancel()
        }
    }

    @Test
    fun `present - when a room is invited then accept and decline events are sent to acceptDeclinePresenter`() = runTest {
        val eventSinkRecorder = lambdaRecorder { _: AcceptDeclineInviteEvents -> }
        val acceptDeclinePresenter = Presenter {
            anAcceptDeclineInviteState(eventSink = eventSinkRecorder)
        }
        val roomListService = FakeRoomListService()
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val matrixClient = FakeMatrixClient(
            roomListService = roomListService,
        )
        val roomSummary = aRoomSummary(
            currentUserMembership = CurrentUserMembership.INVITED
        )
        roomListService.postAllRoomsLoadingState(RoomList.LoadingState.Loaded(1))
        roomListService.postAllRooms(listOf(roomSummary))
        val presenter = createRoomListPresenter(
            coroutineScope = scope,
            client = matrixClient,
            acceptDeclineInvitePresenter = acceptDeclinePresenter
        )
        presenter.test {
            val state = consumeItemsUntilPredicate {
                it.contentState is RoomListContentState.Rooms
            }.last()

            val roomListRoomSummary = state.contentAsRooms().summaries.first {
                it.id == roomSummary.roomId.value
            }
            state.eventSink(RoomListEvents.AcceptInvite(roomListRoomSummary))
            state.eventSink(RoomListEvents.DeclineInvite(roomListRoomSummary))

            val inviteData = roomListRoomSummary.toInviteData()

            assert(eventSinkRecorder)
                .isCalledExactly(2)
                .withSequence(
                    listOf(value(AcceptDeclineInviteEvents.AcceptInvite(inviteData))),
                    listOf(value(AcceptDeclineInviteEvents.DeclineInvite(inviteData))),
                )
        }
    }

    @Test
    fun `present - UpdateVisibleRange subscribes to rooms in visible range`() = runTest {
        val subscribeToVisibleRoomsLambda = lambdaRecorder { _: List<RoomId> -> }
        val roomListService = FakeRoomListService(subscribeToVisibleRoomsLambda = subscribeToVisibleRoomsLambda)
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val matrixClient = FakeMatrixClient(
            roomListService = roomListService,
        )
        val roomSummary = aRoomSummary(
            currentUserMembership = CurrentUserMembership.INVITED
        )
        roomListService.postAllRoomsLoadingState(RoomList.LoadingState.Loaded(1))
        roomListService.postAllRooms(listOf(roomSummary))
        val presenter = createRoomListPresenter(
            coroutineScope = scope,
            client = matrixClient,
        )
        presenter.test {
            val state = consumeItemsUntilPredicate {
                it.contentState is RoomListContentState.Rooms
            }.last()

            state.eventSink(RoomListEvents.UpdateVisibleRange(IntRange(0, 10)))
            subscribeToVisibleRoomsLambda.assertions().isCalledOnce()

            // If called again, it will cancel the current one, which should not result in a test failure
            state.eventSink(RoomListEvents.UpdateVisibleRange(IntRange(0, 11)))
            subscribeToVisibleRoomsLambda.assertions().isCalledExactly(2)
        }
    }

    private fun TestScope.createRoomListPresenter(
        client: MatrixClient = FakeMatrixClient(),
        networkMonitor: NetworkMonitor = FakeNetworkMonitor(),
        snackbarDispatcher: SnackbarDispatcher = SnackbarDispatcher(),
        leaveRoomPresenter: LeaveRoomPresenter = FakeLeaveRoomPresenter(),
        lastMessageTimestampFormatter: LastMessageTimestampFormatter = FakeLastMessageTimestampFormatter().apply {
            givenFormat(A_FORMATTED_DATE)
        },
        roomLastMessageFormatter: RoomLastMessageFormatter = FakeRoomLastMessageFormatter(),
        sessionPreferencesStore: SessionPreferencesStore = InMemorySessionPreferencesStore(),
        featureFlagService: FeatureFlagService = FakeFeatureFlagService(),
        coroutineScope: CoroutineScope,
        migrationScreenPresenter: Presenter<MigrationScreenState> = Presenter { MigrationScreenState(false) },
        analyticsService: AnalyticsService = FakeAnalyticsService(),
        filtersPresenter: Presenter<RoomListFiltersState> = Presenter { aRoomListFiltersState() },
        searchPresenter: Presenter<RoomListSearchState> = Presenter { aRoomListSearchState() },
        acceptDeclineInvitePresenter: Presenter<AcceptDeclineInviteState> = Presenter { anAcceptDeclineInviteState() },
        notificationCleaner: NotificationCleaner = FakeNotificationCleaner(),
    ) = RoomListPresenter(
        client = client,
        networkMonitor = networkMonitor,
        snackbarDispatcher = snackbarDispatcher,
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
        featureFlagService = featureFlagService,
        indicatorService = DefaultIndicatorService(
            sessionVerificationService = client.sessionVerificationService(),
            encryptionService = client.encryptionService(),
        ),
        migrationScreenPresenter = migrationScreenPresenter,
        searchPresenter = searchPresenter,
        sessionPreferencesStore = sessionPreferencesStore,
        filtersPresenter = filtersPresenter,
        analyticsService = analyticsService,
        acceptDeclineInvitePresenter = acceptDeclineInvitePresenter,
        fullScreenIntentPermissionsPresenter = FakeFullScreenIntentPermissionsPresenter(),
        notificationCleaner = notificationCleaner,
    )
}

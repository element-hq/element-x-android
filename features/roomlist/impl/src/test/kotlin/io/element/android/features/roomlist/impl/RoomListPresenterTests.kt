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
import io.element.android.libraries.featureflag.test.InMemorySessionPreferencesStore
import io.element.android.libraries.indicator.impl.DefaultIndicatorService
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.encryption.BackupState
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.sync.SyncState
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.matrix.test.notificationsettings.FakeNotificationSettingsService
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.matrix.test.room.aRoomSummaryFilled
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.libraries.matrix.test.sync.FakeSyncService
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.MutablePresenter
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
            sessionVerificationService.givenCanVerifySession(false)
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
                    aRoomSummaryFilled(
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
    fun `present - handle RecoveryKeyConfirmation last session`() = runTest {
        val scope = CoroutineScope(context = coroutineContext + SupervisorJob())
        val roomListService = FakeRoomListService().apply {
            postAllRoomsLoadingState(RoomList.LoadingState.Loaded(1))
        }
        val presenter = createRoomListPresenter(
            coroutineScope = scope,
            client = FakeMatrixClient(
                encryptionService = FakeEncryptionService().apply {
                    emitIsLastDevice(true)
                },
                roomListService = roomListService
            ),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val eventSink = consumeItemsUntilPredicate {
                it.contentState is RoomListContentState.Rooms
            }.last().eventSink
            // For the last session, the state is not SessionVerification, but RecoveryKeyConfirmation
            assertThat(awaitItem().contentAsRooms().securityBannerState).isEqualTo(SecurityBannerState.RecoveryKeyConfirmation)
            eventSink(RoomListEvents.DismissRequestVerificationPrompt)
            assertThat(awaitItem().contentAsRooms().securityBannerState).isEqualTo(SecurityBannerState.None)
            scope.cancel()
        }
    }

    @Test
    fun `present - handle DismissRequestVerificationPrompt`() = runTest {
        val scope = CoroutineScope(context = coroutineContext + SupervisorJob())
        val roomListService = FakeRoomListService().apply {
            postAllRoomsLoadingState(RoomList.LoadingState.Loaded(1))
        }
        val presenter = createRoomListPresenter(
            client = FakeMatrixClient(roomListService = roomListService),
            coroutineScope = scope,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val eventSink = consumeItemsUntilPredicate {
                it.contentState is RoomListContentState.Rooms
            }.last().eventSink
            assertThat(awaitItem().contentAsRooms().securityBannerState).isEqualTo(SecurityBannerState.SessionVerification)
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
                givenCanVerifySession(false)
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
    fun `present - sets invite state`() = runTest {
        val inviteStateFlow = MutableStateFlow(InvitesState.NoInvites)
        val inviteStateDataSource = FakeInviteDataSource(inviteStateFlow)
        val roomListService = FakeRoomListService()
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(
            inviteStateDataSource = inviteStateDataSource,
            coroutineScope = scope,
            client = FakeMatrixClient(roomListService = roomListService),
        )
        roomListService.postAllRoomsLoadingState(RoomList.LoadingState.Loaded(1))
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            consumeItemsUntilPredicate {
                it.contentState is RoomListContentState.Rooms
            }
            assertThat(awaitItem().contentAsRooms().invitesState).isEqualTo(InvitesState.NoInvites)

            inviteStateFlow.value = InvitesState.SeenInvites
            assertThat(awaitItem().contentAsRooms().invitesState).isEqualTo(InvitesState.SeenInvites)

            inviteStateFlow.value = InvitesState.NewInvites
            assertThat(awaitItem().contentAsRooms().invitesState).isEqualTo(InvitesState.NewInvites)

            inviteStateFlow.value = InvitesState.NoInvites
            assertThat(awaitItem().contentAsRooms().invitesState).isEqualTo(InvitesState.NoInvites)
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
        val sessionPreferencesStore = InMemorySessionPreferencesStore()
        val matrixClient = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, room)
        }
        val analyticsService = FakeAnalyticsService()
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        val presenter = createRoomListPresenter(
            client = matrixClient,
            coroutineScope = scope,
            sessionPreferencesStore = sessionPreferencesStore,
            analyticsService = analyticsService,
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
            assertThat(analyticsService.capturedEvents).containsExactly(
                Interaction(name = Interaction.Name.MobileRoomListRoomContextMenuUnreadToggle),
                Interaction(name = Interaction.Name.MobileRoomListRoomContextMenuUnreadToggle),
                Interaction(name = Interaction.Name.MobileRoomListRoomContextMenuUnreadToggle),
            )
            cancelAndIgnoreRemainingEvents()
            scope.cancel()
        }
    }

    private fun TestScope.createRoomListPresenter(
        client: MatrixClient = FakeMatrixClient(),
        networkMonitor: NetworkMonitor = FakeNetworkMonitor(),
        snackbarDispatcher: SnackbarDispatcher = SnackbarDispatcher(),
        inviteStateDataSource: InviteStateDataSource = FakeInviteDataSource(),
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
    ) = RoomListPresenter(
        client = client,
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
    )
}

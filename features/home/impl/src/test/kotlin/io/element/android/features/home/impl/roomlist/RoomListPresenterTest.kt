/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.roomlist

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.Interaction
import io.element.android.features.announcement.api.Announcement
import io.element.android.features.announcement.api.AnnouncementService
import io.element.android.features.home.impl.FakeDateTimeObserver
import io.element.android.features.home.impl.datasource.RoomListDataSource
import io.element.android.features.home.impl.datasource.aRoomListRoomSummaryFactory
import io.element.android.features.home.impl.filters.RoomListFiltersState
import io.element.android.features.home.impl.filters.aRoomListFiltersState
import io.element.android.features.home.impl.model.createRoomListRoomSummary
import io.element.android.features.home.impl.search.RoomListSearchEvents
import io.element.android.features.home.impl.search.RoomListSearchState
import io.element.android.features.home.impl.search.aRoomListSearchState
import io.element.android.features.invite.api.SeenInvitesStore
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteEvents
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteState
import io.element.android.features.invite.api.acceptdecline.anAcceptDeclineInviteState
import io.element.android.features.invite.test.InMemorySeenInvitesStore
import io.element.android.features.leaveroom.api.LeaveRoomEvent
import io.element.android.features.leaveroom.api.LeaveRoomState
import io.element.android.features.rageshake.test.logs.FakeAnnouncementService
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.dateformatter.api.DateFormatter
import io.element.android.libraries.dateformatter.test.FakeDateFormatter
import io.element.android.libraries.eventformatter.api.RoomLatestEventFormatter
import io.element.android.libraries.eventformatter.test.FakeRoomLatestEventFormatter
import io.element.android.libraries.fullscreenintent.api.aFullScreenIntentPermissionsState
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.sync.SyncState
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_ID_3
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.matrix.test.notificationsettings.FakeNotificationSettingsService
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.libraries.matrix.test.room.aRoomSummary
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.libraries.matrix.test.sync.FakeSyncService
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
import io.element.android.libraries.push.api.battery.aBatteryOptimizationState
import io.element.android.libraries.push.api.notifications.NotificationCleaner
import io.element.android.libraries.push.test.notifications.FakeNotificationCleaner
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.services.analytics.test.watchers.FakeAnalyticsColdStartWatcher
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class RoomListPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - load 1 room with success`() = runTest {
        val roomListService = FakeRoomListService()
        val matrixClient = FakeMatrixClient(
            roomListService = roomListService
        )
        val presenter = createRoomListPresenter(
            client = matrixClient,
            seenInvitesStore = InMemorySeenInvitesStore(setOf(A_ROOM_ID, A_ROOM_ID_2, A_ROOM_ID_3)),
        )
        presenter.test {
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
                    timestamp = "0 TimeOrDate true",
                )
            )
            assertThat(withRoomsState.contentAsRooms().seenRoomInvites).containsExactly(A_ROOM_ID, A_ROOM_ID_2, A_ROOM_ID_3)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - handle DismissRequestVerificationPrompt`() = runTest {
        val roomListService = FakeRoomListService().apply {
            postAllRoomsLoadingState(RoomList.LoadingState.Loaded(1))
        }
        val encryptionService = FakeEncryptionService().apply {
            emitRecoveryState(RecoveryState.INCOMPLETE)
        }
        val syncService = FakeSyncService(initialSyncState = SyncState.Running)
        val presenter = createRoomListPresenter(
            client = FakeMatrixClient(roomListService = roomListService, encryptionService = encryptionService, syncService = syncService),
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
        }
    }

    @Test
    fun `present - handle DismissRecoveryKeyPrompt`() = runTest {
        val encryptionService = FakeEncryptionService().apply {
            recoveryStateStateFlow.emit(RecoveryState.DISABLED)
        }
        val roomListService = FakeRoomListService().apply {
            postAllRoomsLoadingState(RoomList.LoadingState.Loaded(1))
        }
        val matrixClient = FakeMatrixClient(
            roomListService = roomListService,
            encryptionService = encryptionService,
            sessionVerificationService = FakeSessionVerificationService().apply {
                emitNeedsSessionVerification(false)
            },
            syncService = FakeSyncService(initialSyncState = SyncState.Running),
        )
        val presenter = createRoomListPresenter(
            client = matrixClient,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = consumeItemsUntilPredicate {
                it.contentState is RoomListContentState.Rooms
            }.last()
            assertThat(initialState.contentAsRooms().securityBannerState).isEqualTo(SecurityBannerState.SetUpRecovery)
            encryptionService.emitRecoveryState(RecoveryState.INCOMPLETE)
            val nextState = awaitItem()
            assertThat(nextState.contentAsRooms().securityBannerState).isEqualTo(SecurityBannerState.RecoveryKeyConfirmation)
            // Also check other states
            encryptionService.emitRecoveryState(RecoveryState.DISABLED)
            assertThat(awaitItem().contentAsRooms().securityBannerState).isEqualTo(SecurityBannerState.SetUpRecovery)
            encryptionService.emitRecoveryState(RecoveryState.WAITING_FOR_SYNC)
            assertThat(awaitItem().contentAsRooms().securityBannerState).isEqualTo(SecurityBannerState.None)
            encryptionService.emitRecoveryState(RecoveryState.DISABLED)
            assertThat(awaitItem().contentAsRooms().securityBannerState).isEqualTo(SecurityBannerState.SetUpRecovery)
            encryptionService.emitRecoveryState(RecoveryState.ENABLED)
            assertThat(awaitItem().contentAsRooms().securityBannerState).isEqualTo(SecurityBannerState.None)
            encryptionService.emitRecoveryState(RecoveryState.DISABLED)
            assertThat(awaitItem().contentAsRooms().securityBannerState).isEqualTo(SecurityBannerState.SetUpRecovery)
            nextState.eventSink(RoomListEvents.DismissBanner)
            val finalState = awaitItem()
            assertThat(finalState.contentAsRooms().securityBannerState).isEqualTo(SecurityBannerState.None)
        }
    }

    @Test
    fun `present - show context menu`() = runTest {
        val room = FakeBaseRoom()
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, room)
        }
        val presenter = createRoomListPresenter(client = client)
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
                            hasNewContent = false,
                            displayClearRoomCacheAction = false,
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
                            hasNewContent = false,
                            displayClearRoomCacheAction = false,
                        )
                    )
            }
        }
    }

    @Test
    fun `present - show context menu with view source on`() = runTest {
        val presenter = createRoomListPresenter(
            appPreferencesStore = InMemoryAppPreferencesStore(
                isDeveloperModeEnabled = true,
            )
        )
        presenter.test {
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
                            // true here.
                            hasNewContent = false,
                            displayClearRoomCacheAction = true,
                        )
                    )
            }
        }
    }

    @Test
    fun `present - hide context menu`() = runTest {
        val room = FakeBaseRoom()
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, room)
        }
        val presenter = createRoomListPresenter(client = client)
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
                        hasNewContent = false,
                        displayClearRoomCacheAction = false,
                    )
                )

            shownState.eventSink(RoomListEvents.HideContextMenu)

            val hiddenState = awaitItem()
            assertThat(hiddenState.contextMenu).isEqualTo(RoomListState.ContextMenu.Hidden)
        }
    }

    @Test
    fun `present - leave room calls into leave room presenter`() = runTest {
        val leaveRoomEventsRecorder = EventsRecorder<LeaveRoomEvent>()
        val presenter = createRoomListPresenter(
            leaveRoomState = aLeaveRoomState(eventSink = leaveRoomEventsRecorder),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomListEvents.LeaveRoom(A_ROOM_ID, needsConfirmation = true))
            leaveRoomEventsRecorder.assertSingle(LeaveRoomEvent.LeaveRoom(A_ROOM_ID, needsConfirmation = true))
            cancelAndIgnoreRemainingEvents()
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
        val presenter = createRoomListPresenter(
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
        }
    }

    @Test
    fun `present - change in notification settings updates the summary for decorations`() = runTest {
        val userDefinedMode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY
        val notificationSettingsService = FakeNotificationSettingsService()
        val roomListService = FakeRoomListService()
        roomListService.postAllRoomsLoadingState(RoomList.LoadingState.Loaded(1))
        roomListService.postAllRooms(listOf(aRoomSummary(userDefinedNotificationMode = userDefinedMode)))
        val matrixClient = FakeMatrixClient(
            roomListService = roomListService,
            notificationSettingsService = notificationSettingsService
        )
        val presenter = createRoomListPresenter(client = matrixClient)
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
        }
    }

    @Test
    fun `present - when set is favorite event is emitted, then the action is called`() = runTest {
        val setIsFavoriteResult = lambdaRecorder { _: Boolean -> Result.success(Unit) }
        val room = FakeBaseRoom(
            setIsFavoriteResult = setIsFavoriteResult
        )
        val analyticsService = FakeAnalyticsService()
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, room)
        }
        val presenter = createRoomListPresenter(client = client, analyticsService = analyticsService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomListEvents.SetRoomIsFavorite(A_ROOM_ID, true))
            setIsFavoriteResult.assertions().isCalledOnce().with(value(true))
            initialState.eventSink(RoomListEvents.SetRoomIsFavorite(A_ROOM_ID, false))
            setIsFavoriteResult.assertions().isCalledExactly(2)
                .withSequence(
                    listOf(value(true)),
                    listOf(value(false)),
                )
            assertThat(analyticsService.capturedEvents).containsExactly(
                Interaction(name = Interaction.Name.MobileRoomListRoomContextMenuFavouriteToggle),
                Interaction(name = Interaction.Name.MobileRoomListRoomContextMenuFavouriteToggle)
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - when room service returns no room, then contentState is Empty`() = runTest {
        val roomListService = FakeRoomListService()
        roomListService.postAllRoomsLoadingState(RoomList.LoadingState.Loaded(0))
        val matrixClient = FakeMatrixClient(
            roomListService = roomListService,
        )
        val presenter = createRoomListPresenter(
            client = matrixClient,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().contentState).isInstanceOf(RoomListContentState.Empty::class.java)
        }
    }

    @Test
    fun `present - check that the room is marked as read with correct RR and as unread`() = runTest {
        val markAsReadResult = lambdaRecorder<ReceiptType, Result<Unit>> { Result.success(Unit) }
        val markAsReadResult3 = lambdaRecorder<ReceiptType, Result<Unit>> { Result.success(Unit) }
        val room = FakeBaseRoom(
            markAsReadResult = markAsReadResult,
        )
        val room2 = FakeBaseRoom(
            roomId = A_ROOM_ID_2,
        )
        val room3 = FakeBaseRoom(
            roomId = A_ROOM_ID_3,
            markAsReadResult = markAsReadResult3,
        )
        val allRooms = setOf(room, room2, room3)
        val sessionPreferencesStore = InMemorySessionPreferencesStore()
        val matrixClient = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, room)
            givenGetRoomResult(A_ROOM_ID_2, room2)
            givenGetRoomResult(A_ROOM_ID_3, room3)
        }
        val analyticsService = FakeAnalyticsService()
        val clearMessagesForRoomLambda = lambdaRecorder<SessionId, RoomId, Unit> { _, _ -> }
        val notificationCleaner = FakeNotificationCleaner(
            clearMessagesForRoomLambda = clearMessagesForRoomLambda,
        )
        val presenter = createRoomListPresenter(
            client = matrixClient,
            sessionPreferencesStore = sessionPreferencesStore,
            analyticsService = analyticsService,
            notificationCleaner = notificationCleaner,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            allRooms.forEach {
                assertThat(it.setUnreadFlagCalls).isEmpty()
            }
            initialState.eventSink.invoke(RoomListEvents.MarkAsRead(A_ROOM_ID))
            markAsReadResult.assertions().isCalledOnce().with(value(ReceiptType.READ))
            assertThat(room.setUnreadFlagCalls).isEqualTo(listOf(false))
            clearMessagesForRoomLambda.assertions().isCalledOnce()
                .with(value(A_SESSION_ID), value(A_ROOM_ID))
            initialState.eventSink.invoke(RoomListEvents.MarkAsUnread(A_ROOM_ID_2))
            assertThat(room2.setUnreadFlagCalls).isEqualTo(listOf(true))
            // Test again with private read receipts
            sessionPreferencesStore.setSendPublicReadReceipts(false)
            initialState.eventSink.invoke(RoomListEvents.MarkAsRead(A_ROOM_ID_3))
            markAsReadResult3.assertions().isCalledOnce().with(value(ReceiptType.READ_PRIVATE))
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
        }
    }

    @Test
    fun `present - when a room is invited then accept and decline events are sent to acceptDeclinePresenter`() = runTest {
        val eventSinkRecorder = lambdaRecorder { _: AcceptDeclineInviteEvents -> }
        val acceptDeclinePresenter = Presenter {
            anAcceptDeclineInviteState(eventSink = eventSinkRecorder)
        }
        val roomListService = FakeRoomListService()
        val matrixClient = FakeMatrixClient(
            roomListService = roomListService,
        )
        val roomSummary = aRoomSummary(
            currentUserMembership = CurrentUserMembership.INVITED,
            inviter = aRoomMember(),
        )
        roomListService.postAllRoomsLoadingState(RoomList.LoadingState.Loaded(1))
        roomListService.postAllRooms(listOf(roomSummary))
        val presenter = createRoomListPresenter(
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
            state.eventSink(RoomListEvents.DeclineInvite(roomListRoomSummary, blockUser = false))

            val inviteData = roomListRoomSummary.toInviteData()
            assert(eventSinkRecorder)
                .isCalledExactly(2)
                .withSequence(
                    listOf(value(AcceptDeclineInviteEvents.AcceptInvite(inviteData))),
                    listOf(value(AcceptDeclineInviteEvents.DeclineInvite(inviteData, blockUser = false, shouldConfirm = false))),
                )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `present - UpdateVisibleRange will cancel the previous subscription if called too soon`() = runTest {
        val subscribeToVisibleRoomsLambda = lambdaRecorder { _: List<RoomId> -> }
        val roomListService = FakeRoomListService(subscribeToVisibleRoomsLambda = subscribeToVisibleRoomsLambda)
        val matrixClient = FakeMatrixClient(
            roomListService = roomListService,
        )
        val roomSummary = aRoomSummary(
            currentUserMembership = CurrentUserMembership.INVITED
        )
        roomListService.postAllRoomsLoadingState(RoomList.LoadingState.Loaded(1))
        roomListService.postAllRooms(listOf(roomSummary))
        val presenter = createRoomListPresenter(
            client = matrixClient,
        )
        presenter.test {
            val state = consumeItemsUntilPredicate {
                it.contentState is RoomListContentState.Rooms
            }.last()

            state.eventSink(RoomListEvents.UpdateVisibleRange(IntRange(0, 10)))
            // If called again, it will cancel the current one, which should not result in a test failure
            state.eventSink(RoomListEvents.UpdateVisibleRange(IntRange(0, 11)))
            advanceTimeBy(1.seconds)
            subscribeToVisibleRoomsLambda.assertions().isCalledOnce()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `present - UpdateVisibleRange subscribes to rooms in visible range`() = runTest {
        val subscribeToVisibleRoomsLambda = lambdaRecorder { _: List<RoomId> -> }
        val roomListService = FakeRoomListService(subscribeToVisibleRoomsLambda = subscribeToVisibleRoomsLambda)
        val matrixClient = FakeMatrixClient(
            roomListService = roomListService,
        )
        val roomSummary = aRoomSummary(
            currentUserMembership = CurrentUserMembership.INVITED
        )
        roomListService.postAllRoomsLoadingState(RoomList.LoadingState.Loaded(1))
        roomListService.postAllRooms(listOf(roomSummary))
        val presenter = createRoomListPresenter(
            client = matrixClient,
        )
        presenter.test {
            val state = consumeItemsUntilPredicate {
                it.contentState is RoomListContentState.Rooms
            }.last()

            state.eventSink(RoomListEvents.UpdateVisibleRange(IntRange(0, 10)))
            advanceTimeBy(1.seconds)
            subscribeToVisibleRoomsLambda.assertions().isCalledOnce()

            // If called again, it will subscribe to the next items
            state.eventSink(RoomListEvents.UpdateVisibleRange(IntRange(0, 11)))
            advanceTimeBy(1.seconds)
            subscribeToVisibleRoomsLambda.assertions().isCalledExactly(2)
        }
    }

    @Test
    fun `present - notification sound banner`() = runTest {
        val subscribeToVisibleRoomsLambda = lambdaRecorder { _: List<RoomId> -> }
        val roomListService = FakeRoomListService(subscribeToVisibleRoomsLambda = subscribeToVisibleRoomsLambda)
        val matrixClient = FakeMatrixClient(
            roomListService = roomListService,
        )
        val roomSummary = aRoomSummary(
            currentUserMembership = CurrentUserMembership.INVITED
        )
        roomListService.postAllRoomsLoadingState(RoomList.LoadingState.Loaded(1))
        roomListService.postAllRooms(listOf(roomSummary))
        val onAnnouncementDismissedResult = lambdaRecorder<Announcement, Unit> { }
        val announcementService = FakeAnnouncementService(
            onAnnouncementDismissedResult = onAnnouncementDismissedResult,
        )
        val presenter = createRoomListPresenter(
            client = matrixClient,
            announcementService = announcementService,
        )
        presenter.test {
            assertThat(announcementService.announcementsToShowFlow().first()).isEmpty()
            skipItems(1)
            val state = awaitItem()
            assertThat(state.contentAsRooms().showNewNotificationSoundBanner).isFalse()
            announcementService.emitAnnouncementsToShow(listOf(Announcement.NewNotificationSound))
            assertThat(awaitItem().contentAsRooms().showNewNotificationSoundBanner).isTrue()
            state.eventSink(RoomListEvents.DismissNewNotificationSoundBanner)
            onAnnouncementDismissedResult.assertions().isCalledOnce()
                .with(value(Announcement.NewNotificationSound))
            // Simulate service updating the value
            announcementService.emitAnnouncementsToShow(emptyList())
            assertThat(awaitItem().contentAsRooms().showNewNotificationSoundBanner).isFalse()
        }
    }

    private fun TestScope.createRoomListPresenter(
        client: MatrixClient = FakeMatrixClient(),
        leaveRoomState: LeaveRoomState = aLeaveRoomState(),
        dateFormatter: DateFormatter = FakeDateFormatter(),
        roomLatestEventFormatter: RoomLatestEventFormatter = FakeRoomLatestEventFormatter(),
        sessionPreferencesStore: SessionPreferencesStore = InMemorySessionPreferencesStore(),
        analyticsService: AnalyticsService = FakeAnalyticsService(),
        filtersPresenter: Presenter<RoomListFiltersState> = Presenter { aRoomListFiltersState() },
        searchPresenter: Presenter<RoomListSearchState> = Presenter { aRoomListSearchState() },
        acceptDeclineInvitePresenter: Presenter<AcceptDeclineInviteState> = Presenter { anAcceptDeclineInviteState() },
        notificationCleaner: NotificationCleaner = FakeNotificationCleaner(),
        appPreferencesStore: AppPreferencesStore = InMemoryAppPreferencesStore(),
        seenInvitesStore: SeenInvitesStore = InMemorySeenInvitesStore(),
        announcementService: AnnouncementService = FakeAnnouncementService(),
    ) = RoomListPresenter(
        client = client,
        leaveRoomPresenter = { leaveRoomState },
        roomListDataSource = RoomListDataSource(
            roomListService = client.roomListService,
            roomListRoomSummaryFactory = aRoomListRoomSummaryFactory(
                dateFormatter = dateFormatter,
                roomLatestEventFormatter = roomLatestEventFormatter,
            ),
            coroutineDispatchers = testCoroutineDispatchers(),
            notificationSettingsService = client.notificationSettingsService,
            sessionCoroutineScope = backgroundScope,
            dateTimeObserver = FakeDateTimeObserver(),
        ),
        searchPresenter = searchPresenter,
        sessionPreferencesStore = sessionPreferencesStore,
        filtersPresenter = filtersPresenter,
        analyticsService = analyticsService,
        acceptDeclineInvitePresenter = acceptDeclineInvitePresenter,
        fullScreenIntentPermissionsPresenter = { aFullScreenIntentPermissionsState() },
        batteryOptimizationPresenter = { aBatteryOptimizationState() },
        notificationCleaner = notificationCleaner,
        appPreferencesStore = appPreferencesStore,
        seenInvitesStore = seenInvitesStore,
        announcementService = announcementService,
        coldStartWatcher = FakeAnalyticsColdStartWatcher(),
    )
}

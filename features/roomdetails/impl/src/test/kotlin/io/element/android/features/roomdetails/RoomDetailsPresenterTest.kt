/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails

import androidx.lifecycle.Lifecycle
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.Interaction
import io.element.android.features.createroom.test.FakeStartDMAction
import io.element.android.features.leaveroom.api.LeaveRoomEvent
import io.element.android.features.leaveroom.api.LeaveRoomState
import io.element.android.features.leaveroom.api.aLeaveRoomState
import io.element.android.features.roomdetails.impl.RoomDetailsEvent
import io.element.android.features.roomdetails.impl.RoomDetailsPresenter
import io.element.android.features.roomdetails.impl.RoomDetailsState
import io.element.android.features.roomdetails.impl.RoomDetailsType
import io.element.android.features.roomdetails.impl.RoomTopicState
import io.element.android.features.roomdetails.impl.members.aRoomMember
import io.element.android.features.roomdetails.impl.members.details.RoomMemberDetailsPresenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_ROOM_TOPIC
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.notificationsettings.FakeNotificationSettingsService
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.FakeLifecycleOwner
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.testCoroutineDispatchers
import io.element.android.tests.testutils.withFakeLifecycleOwner
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@ExperimentalCoroutinesApi
class RoomDetailsPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val fakeLifecycleOwner = FakeLifecycleOwner().apply {
        givenState(Lifecycle.State.RESUMED)
    }

    private fun TestScope.createRoomDetailsPresenter(
        room: MatrixRoom = aMatrixRoom(),
        leaveRoomState: LeaveRoomState = aLeaveRoomState(),
        dispatchers: CoroutineDispatchers = testCoroutineDispatchers(),
        notificationSettingsService: FakeNotificationSettingsService = FakeNotificationSettingsService(),
        analyticsService: AnalyticsService = FakeAnalyticsService(),
        isPinnedMessagesFeatureEnabled: Boolean = true,
    ): RoomDetailsPresenter {
        val matrixClient = FakeMatrixClient(notificationSettingsService = notificationSettingsService)
        val roomMemberDetailsPresenterFactory = object : RoomMemberDetailsPresenter.Factory {
            override fun create(roomMemberId: UserId): RoomMemberDetailsPresenter {
                return RoomMemberDetailsPresenter(roomMemberId, matrixClient, room, FakeStartDMAction())
            }
        }
        val featureFlagService = FakeFeatureFlagService(
            mapOf(FeatureFlags.NotificationSettings.key to true)
        )
        return RoomDetailsPresenter(
            client = matrixClient,
            room = room,
            featureFlagService = featureFlagService,
            notificationSettingsService = matrixClient.notificationSettingsService(),
            roomMembersDetailsPresenterFactory = roomMemberDetailsPresenterFactory,
            leaveRoomPresenter = { leaveRoomState },
            dispatchers = dispatchers,
            isPinnedMessagesFeatureEnabled = { isPinnedMessagesFeatureEnabled },
            analyticsService = analyticsService,
        )
    }

    private suspend fun RoomDetailsPresenter.test(validate: suspend TurbineTestContext<RoomDetailsState>.() -> Unit) {
        moleculeFlow(RecompositionMode.Immediate) {
            withFakeLifecycleOwner(fakeLifecycleOwner) {
                present()
            }
        }.test(validate = validate)
    }

    @Test
    fun `present - initial state is created from room if roomInfo is null`() = runTest {
        val room = aMatrixRoom(
            canInviteResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            canSendStateResult = { _, _ -> Result.success(true) },
        )
        val presenter = createRoomDetailsPresenter(room)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.roomId).isEqualTo(room.roomId)
            assertThat(initialState.roomName).isEqualTo(room.displayName)
            assertThat(initialState.roomAvatarUrl).isEqualTo(room.avatarUrl)
            assertThat(initialState.roomTopic).isEqualTo(RoomTopicState.ExistingTopic(room.topic!!))
            assertThat(initialState.memberCount).isEqualTo(room.joinedMemberCount)
            assertThat(initialState.isEncrypted).isEqualTo(room.isEncrypted)
            assertThat(initialState.canShowPinnedMessages).isTrue()
            assertThat(initialState.pinnedMessagesCount).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - initial state is updated with roomInfo if it exists`() = runTest {
        val roomInfo = aRoomInfo(
            name = A_ROOM_NAME,
            topic = A_ROOM_TOPIC,
            avatarUrl = AN_AVATAR_URL,
            pinnedEventIds = listOf(AN_EVENT_ID),
        )
        val room = aMatrixRoom(
            canInviteResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            canSendStateResult = { _, _ -> Result.success(true) },
        ).apply {
            givenRoomInfo(roomInfo)
        }
        val presenter = createRoomDetailsPresenter(room)
        presenter.test {
            skipItems(1)
            val updatedState = awaitItem()
            assertThat(updatedState.roomName).isEqualTo(roomInfo.name)
            assertThat(updatedState.roomAvatarUrl).isEqualTo(roomInfo.avatarUrl)
            assertThat(updatedState.roomTopic).isEqualTo(RoomTopicState.ExistingTopic(roomInfo.topic!!))
            assertThat(updatedState.pinnedMessagesCount).isEqualTo(roomInfo.pinnedEventIds.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - initial state with no room name`() = runTest {
        val room = aMatrixRoom(
            displayName = "",
            canInviteResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            canSendStateResult = { _, _ -> Result.success(true) },
        )
        val presenter = createRoomDetailsPresenter(room)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.roomName).isEqualTo(room.displayName)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - initial state with DM member sets custom DM roomType`() = runTest {
        val myRoomMember = aRoomMember(A_SESSION_ID)
        val otherRoomMember = aRoomMember(A_USER_ID_2)
        val room = aMatrixRoom(
            isEncrypted = true,
            isDirect = true,
            canInviteResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            canSendStateResult = { _, _ -> Result.success(true) },
            getUpdatedMemberResult = { userId ->
                when (userId) {
                    A_SESSION_ID -> Result.success(myRoomMember)
                    A_USER_ID_2 -> Result.success(otherRoomMember)
                    else -> lambdaError()
                }
            },
        ).apply {
            val roomMembers = persistentListOf(myRoomMember, otherRoomMember)
            givenRoomMembersState(MatrixRoomMembersState.Ready(roomMembers))
        }
        val presenter = createRoomDetailsPresenter(room)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.roomType).isEqualTo(
                RoomDetailsType.Dm(
                    me = myRoomMember,
                    otherMember = otherRoomMember,
                )
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - initial state when user can invite others to room`() = runTest {
        val room = aMatrixRoom(
            canInviteResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            canSendStateResult = { _, _ -> Result.success(true) },
        )
        val presenter = createRoomDetailsPresenter(room, dispatchers = testCoroutineDispatchers())
        presenter.test {
            // Initially false
            assertThat(awaitItem().canInvite).isFalse()
            // Then the asynchronous check completes and it becomes true
            assertThat(awaitItem().canInvite).isTrue()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - initial state when user can not invite others to room`() = runTest {
        val room = aMatrixRoom(
            canInviteResult = { Result.success(false) },
            canUserJoinCallResult = { Result.success(true) },
            canSendStateResult = { _, _ -> Result.success(true) },
        )
        val presenter = createRoomDetailsPresenter(room)
        presenter.test {
            assertThat(awaitItem().canInvite).isFalse()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - initial state when canInvite errors`() = runTest {
        val room = aMatrixRoom(
            canInviteResult = { Result.failure(Throwable("Whoops")) },
            canUserJoinCallResult = { Result.success(true) },
            canSendStateResult = { _, _ -> Result.success(true) },
        )
        val presenter = createRoomDetailsPresenter(room)
        presenter.test {
            assertThat(awaitItem().canInvite).isFalse()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - initial state when user can edit one attribute`() = runTest {
        val room = aMatrixRoom(
            canSendStateResult = { _, stateEventType ->
                when (stateEventType) {
                    StateEventType.ROOM_TOPIC -> Result.success(true)
                    StateEventType.ROOM_NAME -> Result.success(false)
                    StateEventType.ROOM_AVATAR -> Result.failure(Throwable("Whelp"))
                    else -> lambdaError()
                }
            },
            canInviteResult = { Result.success(false) },
            canUserJoinCallResult = { Result.success(true) },
        )
        val presenter = createRoomDetailsPresenter(room)
        presenter.test {
            // Initially false
            assertThat(awaitItem().canEdit).isFalse()
            // Then the asynchronous check completes and it becomes true
            assertThat(awaitItem().canEdit).isTrue()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - initial state when user can edit attributes in a DM`() = runTest {
        val myRoomMember = aRoomMember(A_SESSION_ID)
        val otherRoomMember = aRoomMember(A_USER_ID_2)
        val room = aMatrixRoom(
            isEncrypted = true,
            isDirect = true,
            canSendStateResult = { _, stateEventType ->
                when (stateEventType) {
                    StateEventType.ROOM_TOPIC -> Result.success(true)
                    StateEventType.ROOM_NAME -> Result.success(true)
                    StateEventType.ROOM_AVATAR -> Result.success(true)
                    else -> lambdaError()
                }
            },
            canInviteResult = { Result.success(false) },
            canUserJoinCallResult = { Result.success(true) },
            getUpdatedMemberResult = { userId ->
                when (userId) {
                    A_SESSION_ID -> Result.success(myRoomMember)
                    A_USER_ID_2 -> Result.success(otherRoomMember)
                    else -> lambdaError()
                }
            },
        ).apply {
            val roomMembers = persistentListOf(myRoomMember, otherRoomMember)
            givenRoomMembersState(MatrixRoomMembersState.Ready(roomMembers))
        }
        val presenter = createRoomDetailsPresenter(room)
        presenter.test {
            // Initially false
            assertThat(awaitItem().canEdit).isFalse()
            // Then the asynchronous check completes, but editing is still disallowed because it's a DM
            val settledState = awaitItem()
            assertThat(settledState.canEdit).isFalse()
            // If there is a topic, it's visible
            assertThat(settledState.roomTopic).isEqualTo(RoomTopicState.ExistingTopic(room.topic!!))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - initial state when in a DM with no topic`() = runTest {
        val myRoomMember = aRoomMember(A_SESSION_ID)
        val otherRoomMember = aRoomMember(A_USER_ID_2)
        val room = aMatrixRoom(
            isEncrypted = true,
            isDirect = true,
            topic = null,
            canSendStateResult = { _, stateEventType ->
                when (stateEventType) {
                    StateEventType.ROOM_AVATAR,
                    StateEventType.ROOM_TOPIC,
                    StateEventType.ROOM_NAME -> Result.success(true)
                    else -> lambdaError()
                }
            },
            canInviteResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            getUpdatedMemberResult = { userId ->
                when (userId) {
                    A_SESSION_ID -> Result.success(myRoomMember)
                    A_USER_ID_2 -> Result.success(otherRoomMember)
                    else -> lambdaError()
                }
            },
        ).apply {
            val roomMembers = persistentListOf(myRoomMember, otherRoomMember)
            givenRoomMembersState(MatrixRoomMembersState.Ready(roomMembers))
        }

        val presenter = createRoomDetailsPresenter(room)
        presenter.test {
            skipItems(1)

            // There's no topic, so we hide the entire UI for DMs
            assertThat(awaitItem().roomTopic).isEqualTo(RoomTopicState.Hidden)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - initial state when user can edit all attributes`() = runTest {
        val room = aMatrixRoom(
            canSendStateResult = { _, stateEventType ->
                when (stateEventType) {
                    StateEventType.ROOM_TOPIC -> Result.success(true)
                    StateEventType.ROOM_NAME -> Result.success(true)
                    StateEventType.ROOM_AVATAR -> Result.success(true)
                    else -> lambdaError()
                }
            },
            canInviteResult = {
                Result.success(false)
            },
            canUserJoinCallResult = { Result.success(true) },
        )
        val presenter = createRoomDetailsPresenter(room)
        presenter.test {
            // Initially false
            assertThat(awaitItem().canEdit).isFalse()
            // Then the asynchronous check completes and it becomes true
            assertThat(awaitItem().canEdit).isTrue()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - initial state when user can edit no attributes`() = runTest {
        val room = aMatrixRoom(
            canSendStateResult = { _, stateEventType ->
                when (stateEventType) {
                    StateEventType.ROOM_TOPIC -> Result.success(false)
                    StateEventType.ROOM_NAME -> Result.success(false)
                    StateEventType.ROOM_AVATAR -> Result.success(false)
                    else -> lambdaError()
                }
            },
            canInviteResult = {
                Result.success(false)
            },
            canUserJoinCallResult = { Result.success(true) },
        )
        val presenter = createRoomDetailsPresenter(room)
        presenter.test {
            // Initially false, and no further events
            assertThat(awaitItem().canEdit).isFalse()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - topic state is hidden when no topic and user has no permission`() = runTest {
        val room = aMatrixRoom(
            topic = null,
            canSendStateResult = { _, stateEventType ->
                when (stateEventType) {
                    StateEventType.ROOM_AVATAR,
                    StateEventType.ROOM_NAME -> Result.success(true)
                    StateEventType.ROOM_TOPIC -> Result.success(false)
                    else -> lambdaError()
                }
            },
            canInviteResult = {
                Result.success(false)
            },
            canUserJoinCallResult = { Result.success(true) },
        )
        val presenter = createRoomDetailsPresenter(room)
        presenter.test {
            // The initial state is "hidden" and no further state changes happen
            assertThat(awaitItem().roomTopic).isEqualTo(RoomTopicState.Hidden)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - topic state is 'can add topic' when no topic and user has permission`() = runTest {
        val room = aMatrixRoom(
            topic = null,
            canSendStateResult = { _, stateEventType ->
                when (stateEventType) {
                    StateEventType.ROOM_AVATAR,
                    StateEventType.ROOM_TOPIC,
                    StateEventType.ROOM_NAME -> Result.success(true)
                    else -> lambdaError()
                }
            },
            canInviteResult = {
                Result.success(false)
            },
            canUserJoinCallResult = { Result.success(true) },
        ).apply {
            givenRoomInfo(aRoomInfo(topic = null))
        }
        val presenter = createRoomDetailsPresenter(room)
        presenter.test {
            // Ignore the initial state
            skipItems(1)

            // When the async permission check finishes, the topic state will be updated
            assertThat(awaitItem().roomTopic).isEqualTo(RoomTopicState.CanAddTopic)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - leave room event is passed on to leave room presenter`() = runTest {
        val leaveRoomEventRecorder = EventsRecorder<LeaveRoomEvent>()
        val room = aMatrixRoom(
            canInviteResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            canSendStateResult = { _, _ -> Result.success(true) },
        )
        val presenter = createRoomDetailsPresenter(
            room = room,
            leaveRoomState = aLeaveRoomState(eventSink = leaveRoomEventRecorder),
            dispatchers = testCoroutineDispatchers()
        )
        presenter.test {
            awaitItem().eventSink(RoomDetailsEvent.LeaveRoom)
            leaveRoomEventRecorder.assertSingle(LeaveRoomEvent.ShowConfirmation(room.roomId))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - notification mode changes`() = runTest {
        val notificationSettingsService = FakeNotificationSettingsService()
        val room = aMatrixRoom(
            notificationSettingsService = notificationSettingsService,
            canInviteResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            canSendStateResult = { _, _ -> Result.success(true) },
        )
        val presenter = createRoomDetailsPresenter(
            room = room,
            notificationSettingsService = notificationSettingsService,
        )
        presenter.test {
            notificationSettingsService.setRoomNotificationMode(
                room.roomId,
                RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY
            )
            val updatedState = consumeItemsUntilPredicate {
                it.roomNotificationSettings?.mode == RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY
            }.last()
            assertThat(updatedState.roomNotificationSettings?.mode).isEqualTo(
                RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - mute room notifications`() = runTest {
        val notificationSettingsService =
            FakeNotificationSettingsService(initialRoomMode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY)
        val room = aMatrixRoom(
            notificationSettingsService = notificationSettingsService,
            canInviteResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            canSendStateResult = { _, _ -> Result.success(true) },
        )
        val presenter = createRoomDetailsPresenter(
            room = room,
            notificationSettingsService = notificationSettingsService
        )
        presenter.test {
            awaitItem().eventSink(RoomDetailsEvent.MuteNotification)
            val updatedState = consumeItemsUntilPredicate(timeout = 250.milliseconds) {
                it.roomNotificationSettings?.mode == RoomNotificationMode.MUTE
            }.last()
            assertThat(updatedState.roomNotificationSettings?.mode).isEqualTo(
                RoomNotificationMode.MUTE
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - unmute room notifications`() = runTest {
        val notificationSettingsService = FakeNotificationSettingsService(
            initialRoomMode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY,
            initialEncryptedGroupDefaultMode = RoomNotificationMode.ALL_MESSAGES
        )
        val room = aMatrixRoom(
            notificationSettingsService = notificationSettingsService,
            canInviteResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            canSendStateResult = { _, _ -> Result.success(true) },
        )
        val presenter = createRoomDetailsPresenter(
            room = room,
            notificationSettingsService = notificationSettingsService
        )
        presenter.test {
            awaitItem().eventSink(RoomDetailsEvent.UnmuteNotification)
            val updatedState = consumeItemsUntilPredicate {
                it.roomNotificationSettings?.mode == RoomNotificationMode.ALL_MESSAGES
            }.last()
            assertThat(updatedState.roomNotificationSettings?.mode).isEqualTo(
                RoomNotificationMode.ALL_MESSAGES
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - when set is favorite event is emitted, then the action is called`() = runTest {
        val setIsFavoriteResult = lambdaRecorder<Boolean, Result<Unit>> { _ -> Result.success(Unit) }
        val room = FakeMatrixRoom(
            setIsFavoriteResult = setIsFavoriteResult,
            canInviteResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            canSendStateResult = { _, _ -> Result.success(true) },
        )
        val analyticsService = FakeAnalyticsService()
        val presenter =
            createRoomDetailsPresenter(room = room, analyticsService = analyticsService)
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomDetailsEvent.SetFavorite(true))
            setIsFavoriteResult.assertions().isCalledOnce().with(value(true))
            initialState.eventSink(RoomDetailsEvent.SetFavorite(false))
            setIsFavoriteResult.assertions().isCalledExactly(2)
                .withSequence(
                    listOf(value(true)),
                    listOf(value(false)),
                )
            assertThat(analyticsService.capturedEvents).containsExactly(
                Interaction(name = Interaction.Name.MobileRoomFavouriteToggle),
                Interaction(name = Interaction.Name.MobileRoomFavouriteToggle)
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - changes in room info updates the is favorite flag`() = runTest {
        val room = aMatrixRoom(
            canInviteResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            canSendStateResult = { _, _ -> Result.success(true) },
        )
        val presenter = createRoomDetailsPresenter(room = room)
        presenter.test {
            room.givenRoomInfo(aRoomInfo(isFavorite = true))
            consumeItemsUntilPredicate { it.isFavorite }.last().let { state ->
                assertThat(state.isFavorite).isTrue()
            }
            room.givenRoomInfo(aRoomInfo(isFavorite = false))
            consumeItemsUntilPredicate { !it.isFavorite }.last().let { state ->
                assertThat(state.isFavorite).isFalse()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }
}

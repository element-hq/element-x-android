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
import io.element.android.features.leaveroom.api.LeaveRoomPresenter
import io.element.android.features.leaveroom.fake.FakeLeaveRoomPresenter
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
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.notificationsettings.FakeNotificationSettingsService
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.FakeLifecycleOwner
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.consumeItemsUntilPredicate
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
class RoomDetailsPresenterTests {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val fakeLifecycleOwner = FakeLifecycleOwner().apply {
        givenState(Lifecycle.State.RESUMED)
    }

    private fun TestScope.createRoomDetailsPresenter(
        room: MatrixRoom = aMatrixRoom(),
        leaveRoomPresenter: LeaveRoomPresenter = FakeLeaveRoomPresenter(),
        dispatchers: CoroutineDispatchers = testCoroutineDispatchers(),
        notificationSettingsService: FakeNotificationSettingsService = FakeNotificationSettingsService(),
        analyticsService: AnalyticsService = FakeAnalyticsService(),
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
            leaveRoomPresenter = leaveRoomPresenter,
            dispatchers = dispatchers,
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
        val room = aMatrixRoom()
        val presenter = createRoomDetailsPresenter(room)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.roomId).isEqualTo(room.roomId)
            assertThat(initialState.roomName).isEqualTo(room.displayName)
            assertThat(initialState.roomAvatarUrl).isEqualTo(room.avatarUrl)
            assertThat(initialState.roomTopic).isEqualTo(RoomTopicState.ExistingTopic(room.topic!!))
            assertThat(initialState.memberCount).isEqualTo(room.joinedMemberCount)
            assertThat(initialState.isEncrypted).isEqualTo(room.isEncrypted)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - initial state is updated with roomInfo if it exists`() = runTest {
        val roomInfo = aRoomInfo(name = "A room name", topic = "A topic", avatarUrl = "https://matrix.org/avatar.jpg")
        val room = aMatrixRoom().apply {
            givenRoomInfo(roomInfo)
        }
        val presenter = createRoomDetailsPresenter(room)
        presenter.test {
            skipItems(1)
            val updatedState = awaitItem()
            assertThat(updatedState.roomName).isEqualTo(roomInfo.name)
            assertThat(updatedState.roomAvatarUrl).isEqualTo(roomInfo.avatarUrl)
            assertThat(updatedState.roomTopic).isEqualTo(RoomTopicState.ExistingTopic(roomInfo.topic!!))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - initial state with no room name`() = runTest {
        val room = aMatrixRoom(displayName = "")
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
        ).apply {
            val roomMembers = persistentListOf(myRoomMember, otherRoomMember)
            givenRoomMembersState(MatrixRoomMembersState.Ready(roomMembers))
        }
        val presenter = createRoomDetailsPresenter(room)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.roomType).isEqualTo(RoomDetailsType.Dm(otherRoomMember))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - initial state when user can invite others to room`() = runTest {
        val room = aMatrixRoom().apply {
            givenCanInviteResult(Result.success(true))
        }
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
        val room = aMatrixRoom().apply {
            givenCanInviteResult(Result.success(false))
        }
        val presenter = createRoomDetailsPresenter(room)
        presenter.test {
            assertThat(awaitItem().canInvite).isFalse()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - initial state when canInvite errors`() = runTest {
        val room = aMatrixRoom().apply {
            givenCanInviteResult(Result.failure(Throwable("Whoops")))
        }
        val presenter = createRoomDetailsPresenter(room)
        presenter.test {
            assertThat(awaitItem().canInvite).isFalse()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - initial state when user can edit one attribute`() = runTest {
        val room = aMatrixRoom().apply {
            givenCanSendStateResult(StateEventType.ROOM_TOPIC, Result.success(true))
            givenCanSendStateResult(StateEventType.ROOM_NAME, Result.success(false))
            givenCanSendStateResult(StateEventType.ROOM_AVATAR, Result.failure(Throwable("Whelp")))
            givenCanInviteResult(Result.success(false))
        }
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
        ).apply {
            val roomMembers = persistentListOf(myRoomMember, otherRoomMember)
            givenRoomMembersState(MatrixRoomMembersState.Ready(roomMembers))

            givenCanSendStateResult(StateEventType.ROOM_TOPIC, Result.success(true))
            givenCanSendStateResult(StateEventType.ROOM_NAME, Result.success(true))
            givenCanSendStateResult(StateEventType.ROOM_AVATAR, Result.success(true))
            givenCanInviteResult(Result.success(false))
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
        ).apply {
            val roomMembers = persistentListOf(myRoomMember, otherRoomMember)
            givenRoomMembersState(MatrixRoomMembersState.Ready(roomMembers))

            givenCanSendStateResult(StateEventType.ROOM_TOPIC, Result.success(true))
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
        val room = aMatrixRoom().apply {
            givenCanSendStateResult(StateEventType.ROOM_TOPIC, Result.success(true))
            givenCanSendStateResult(StateEventType.ROOM_NAME, Result.success(true))
            givenCanSendStateResult(StateEventType.ROOM_AVATAR, Result.success(true))
            givenCanInviteResult(Result.success(false))
        }
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
        val room = aMatrixRoom().apply {
            givenCanSendStateResult(StateEventType.ROOM_TOPIC, Result.success(false))
            givenCanSendStateResult(StateEventType.ROOM_NAME, Result.success(false))
            givenCanSendStateResult(StateEventType.ROOM_AVATAR, Result.success(false))
            givenCanInviteResult(Result.success(false))
        }
        val presenter = createRoomDetailsPresenter(room)
        presenter.test {
            // Initially false, and no further events
            assertThat(awaitItem().canEdit).isFalse()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - topic state is hidden when no topic and user has no permission`() = runTest {
        val room = aMatrixRoom(topic = null).apply {
            givenCanSendStateResult(StateEventType.ROOM_TOPIC, Result.success(false))
            givenCanInviteResult(Result.success(false))
        }

        val presenter = createRoomDetailsPresenter(room)
        presenter.test {
            // The initial state is "hidden" and no further state changes happen
            assertThat(awaitItem().roomTopic).isEqualTo(RoomTopicState.Hidden)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - topic state is 'can add topic' when no topic and user has permission`() = runTest {
        val room = aMatrixRoom(topic = null).apply {
            givenCanSendStateResult(StateEventType.ROOM_TOPIC, Result.success(true))
            givenCanInviteResult(Result.success(false))
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
        val leaveRoomPresenter = FakeLeaveRoomPresenter()
        val room = aMatrixRoom()
        val presenter = createRoomDetailsPresenter(
            room = room,
            leaveRoomPresenter = leaveRoomPresenter,
            dispatchers = testCoroutineDispatchers()
        )
        presenter.test {
            awaitItem().eventSink(RoomDetailsEvent.LeaveRoom)

            assertThat(leaveRoomPresenter.events).contains(LeaveRoomEvent.ShowConfirmation(room.roomId))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - notification mode changes`() = runTest {
        val leaveRoomPresenter = FakeLeaveRoomPresenter()
        val notificationSettingsService = FakeNotificationSettingsService()
        val room = aMatrixRoom(notificationSettingsService = notificationSettingsService)
        val presenter = createRoomDetailsPresenter(
            room = room,
            leaveRoomPresenter = leaveRoomPresenter,
            notificationSettingsService = notificationSettingsService,
        )
        presenter.test {
            notificationSettingsService.setRoomNotificationMode(room.roomId, RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY)
            val updatedState = consumeItemsUntilPredicate {
                it.roomNotificationSettings?.mode == RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY
            }.last()
            assertThat(updatedState.roomNotificationSettings?.mode).isEqualTo(RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - mute room notifications`() = runTest {
        val notificationSettingsService = FakeNotificationSettingsService(initialRoomMode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY)
        val room = aMatrixRoom(notificationSettingsService = notificationSettingsService)
        val presenter = createRoomDetailsPresenter(room = room, notificationSettingsService = notificationSettingsService)
        presenter.test {
            awaitItem().eventSink(RoomDetailsEvent.MuteNotification)
            val updatedState = consumeItemsUntilPredicate(timeout = 250.milliseconds) {
                it.roomNotificationSettings?.mode == RoomNotificationMode.MUTE
            }.last()
            assertThat(updatedState.roomNotificationSettings?.mode).isEqualTo(RoomNotificationMode.MUTE)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - unmute room notifications`() = runTest {
        val notificationSettingsService = FakeNotificationSettingsService(
            initialRoomMode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY,
            initialEncryptedGroupDefaultMode = RoomNotificationMode.ALL_MESSAGES
        )
        val room = aMatrixRoom(notificationSettingsService = notificationSettingsService)
        val presenter = createRoomDetailsPresenter(room = room, notificationSettingsService = notificationSettingsService)
        presenter.test {
            awaitItem().eventSink(RoomDetailsEvent.UnmuteNotification)
            val updatedState = consumeItemsUntilPredicate {
                it.roomNotificationSettings?.mode == RoomNotificationMode.ALL_MESSAGES
            }.last()
            assertThat(updatedState.roomNotificationSettings?.mode).isEqualTo(RoomNotificationMode.ALL_MESSAGES)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - when set is favorite event is emitted, then the action is called`() = runTest {
        val room = FakeMatrixRoom()
        val analyticsService = FakeAnalyticsService()
        val presenter = createRoomDetailsPresenter(room = room, analyticsService = analyticsService)
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomDetailsEvent.SetFavorite(true))
            assertThat(room.setIsFavoriteCalls).isEqualTo(listOf(true))
            initialState.eventSink(RoomDetailsEvent.SetFavorite(false))
            assertThat(room.setIsFavoriteCalls).isEqualTo(listOf(true, false))
            assertThat(analyticsService.capturedEvents).containsExactly(
                Interaction(name = Interaction.Name.MobileRoomFavouriteToggle),
                Interaction(name = Interaction.Name.MobileRoomFavouriteToggle)
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - changes in room info updates the is favorite flag`() = runTest {
        val room = aMatrixRoom()
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

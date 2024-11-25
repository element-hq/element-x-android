/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.userprofile

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.userprofile.api.UserProfileEvents
import io.element.android.features.userprofile.api.UserProfileState
import io.element.android.features.userprofile.shared.R
import io.element.android.features.userprofile.shared.UserProfileView
import io.element.android.features.userprofile.shared.aUserProfileState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EnsureNeverCalledWithTwoParams
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.ensureCalledOnceWithTwoParams
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.pressBackKey
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class UserProfileViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `on back key press - the expected callback is called`() = runTest {
        ensureCalledOnce { callback ->
            rule.setUserProfileView(
                goBack = callback,
            )
            rule.pressBackKey()
        }
    }

    @Test
    fun `on back button click - the expected callback is called`() = runTest {
        ensureCalledOnce { callback ->
            rule.setUserProfileView(
                goBack = callback,
            )
            rule.pressBack()
        }
    }

    @Test
    fun `on avatar clicked - the expected callback is called`() = runTest {
        ensureCalledOnceWithTwoParams(A_USER_NAME, AN_AVATAR_URL) { callback ->
            rule.setUserProfileView(
                state = aUserProfileState(userName = A_USER_NAME, avatarUrl = AN_AVATAR_URL),
                openAvatarPreview = callback,
            )
            rule.onNode(hasTestTag(TestTags.memberDetailAvatar.value)).performClick()
        }
    }

    @Test
    fun `on avatar clicked with no avatar - nothing happens`() = runTest {
        val callback = EnsureNeverCalledWithTwoParams<String, String>()
        rule.setUserProfileView(
            state = aUserProfileState(userName = A_USER_NAME, avatarUrl = null),
            openAvatarPreview = callback,
        )
        rule.onNode(hasTestTag(TestTags.memberDetailAvatar.value)).performClick()
    }

    @Test
    fun `on Share clicked - the expected callback is called`() = runTest {
        ensureCalledOnce { callback ->
            rule.setUserProfileView(
                onShareUser = callback,
            )
            rule.clickOn(CommonStrings.action_share)
        }
    }

    @Test
    fun `on Message clicked - the StartDm event is emitted`() = runTest {
        val eventsRecorder = EventsRecorder<UserProfileEvents>()
        rule.setUserProfileView(
            state = aUserProfileState(
                dmRoomId = A_ROOM_ID,
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_message)
        eventsRecorder.assertSingle(UserProfileEvents.StartDM)
    }

    @Test
    fun `on Call clicked - the expected callback is called`() = runTest {
        ensureCalledOnceWithParam(A_ROOM_ID) { callback ->
            rule.setUserProfileView(
                state = aUserProfileState(
                    dmRoomId = A_ROOM_ID,
                    canCall = true,
                ),
                onStartCall = callback,
            )
            rule.clickOn(CommonStrings.action_call)
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `on Block user clicked - a BlockUser event is emitted with needsConfirmation`() = runTest {
        val eventsRecorder = EventsRecorder<UserProfileEvents>()
        rule.setUserProfileView(
            state = aUserProfileState(
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(R.string.screen_dm_details_block_user)
        eventsRecorder.assertSingle(UserProfileEvents.BlockUser(needsConfirmation = true))
    }

    @Test
    fun `on confirming block user - a BlockUser event is emitted without needsConfirmation`() = runTest {
        val eventsRecorder = EventsRecorder<UserProfileEvents>()
        rule.setUserProfileView(
            state = aUserProfileState(
                displayConfirmationDialog = UserProfileState.ConfirmationDialog.Block,
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(R.string.screen_dm_details_block_alert_action)
        eventsRecorder.assertSingle(UserProfileEvents.BlockUser(needsConfirmation = false))
    }

    @Test
    fun `on canceling blocking a user - a ClearConfirmationDialog event is emitted`() = runTest {
        val eventsRecorder = EventsRecorder<UserProfileEvents>()
        rule.setUserProfileView(
            state = aUserProfileState(
                displayConfirmationDialog = UserProfileState.ConfirmationDialog.Block,
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(UserProfileEvents.ClearConfirmationDialog)
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `on Unblock user clicked - an UnblockUser event is emitted with needsConfirmation`() = runTest {
        val eventsRecorder = EventsRecorder<UserProfileEvents>()
        rule.setUserProfileView(
            state = aUserProfileState(
                isBlocked = AsyncData.Success(true),
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(R.string.screen_dm_details_unblock_user)
        eventsRecorder.assertSingle(UserProfileEvents.UnblockUser(needsConfirmation = true))
    }

    @Test
    fun `on confirming Unblock user - an UnblockUser event is emitted without needsConfirmation`() = runTest {
        val eventsRecorder = EventsRecorder<UserProfileEvents>()
        rule.setUserProfileView(
            state = aUserProfileState(
                isBlocked = AsyncData.Success(true),
                displayConfirmationDialog = UserProfileState.ConfirmationDialog.Unblock,
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(R.string.screen_dm_details_unblock_alert_action)
        eventsRecorder.assertSingle(UserProfileEvents.UnblockUser(needsConfirmation = false))
    }

    @Test
    fun `on canceling unblocking a user - a ClearConfirmationDialog event is emitted`() = runTest {
        val eventsRecorder = EventsRecorder<UserProfileEvents>()
        rule.setUserProfileView(
            state = aUserProfileState(
                isBlocked = AsyncData.Success(true),
                displayConfirmationDialog = UserProfileState.ConfirmationDialog.Unblock,
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(UserProfileEvents.ClearConfirmationDialog)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setUserProfileView(
    state: UserProfileState = aUserProfileState(
        eventSink = EventsRecorder(expectEvents = false),
    ),
    onShareUser: () -> Unit = EnsureNeverCalled(),
    onDmStarted: (RoomId) -> Unit = EnsureNeverCalledWithParam(),
    onStartCall: (RoomId) -> Unit = EnsureNeverCalledWithParam(),
    goBack: () -> Unit = EnsureNeverCalled(),
    openAvatarPreview: (String, String) -> Unit = EnsureNeverCalledWithTwoParams(),
) {
    setContent {
        UserProfileView(
            state = state,
            onShareUser = onShareUser,
            onOpenDm = onDmStarted,
            onStartCall = onStartCall,
            goBack = goBack,
            openAvatarPreview = openAvatarPreview,
        )
    }
}

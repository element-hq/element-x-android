/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.userprofile.shared.blockuser

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.userprofile.api.UserProfileEvents
import io.element.android.features.userprofile.api.UserProfileState
import io.element.android.features.userprofile.shared.R
import io.element.android.features.userprofile.shared.aUserProfileState
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BlockUserDialogsTest {
    @Test
    fun `confirm block user emit expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<UserProfileEvents>()
        setContent {
            BlockUserDialogs(
                state = aUserProfileState(
                    displayConfirmationDialog = UserProfileState.ConfirmationDialog.Block,
                    eventSink = eventsRecorder,
                )
            )
        }
        clickOn(R.string.screen_dm_details_block_alert_action)
        eventsRecorder.assertSingle(UserProfileEvents.BlockUser(false))
    }

    @Test
    fun `cancel block user emit expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<UserProfileEvents>()
        setContent {
            BlockUserDialogs(
                state = aUserProfileState(
                    displayConfirmationDialog = UserProfileState.ConfirmationDialog.Block,
                    eventSink = eventsRecorder,
                )
            )
        }
        clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(UserProfileEvents.ClearConfirmationDialog)
    }

    @Test
    fun `confirm unblock user emit expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<UserProfileEvents>()
        setContent {
            BlockUserDialogs(
                state = aUserProfileState(
                    displayConfirmationDialog = UserProfileState.ConfirmationDialog.Unblock,
                    eventSink = eventsRecorder,
                )
            )
        }
        clickOn(R.string.screen_dm_details_unblock_alert_action)
        eventsRecorder.assertSingle(UserProfileEvents.UnblockUser(false))
    }

    @Test
    fun `cancel unblock user emit expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<UserProfileEvents>()
        setContent {
            BlockUserDialogs(
                state = aUserProfileState(
                    displayConfirmationDialog = UserProfileState.ConfirmationDialog.Unblock,
                    eventSink = eventsRecorder,
                )
            )
        }
        clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(UserProfileEvents.ClearConfirmationDialog)
    }
}

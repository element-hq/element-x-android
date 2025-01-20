/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.details

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class MediaDetailsBottomSheetTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on View in timeline invokes expected callback`() {
        val state = aMediaDetailsBottomSheetState()
        ensureCalledOnceWithParam(state.eventId) { callback ->
            rule.setMediaDetailsBottomSheet(
                state = state,
                onViewInTimeline = callback,
            )
            rule.clickOn(CommonStrings.action_view_in_timeline)
        }
    }

    @Test
    fun `clicking on Share invokes expected callback`() {
        val state = aMediaDetailsBottomSheetState()
        ensureCalledOnceWithParam(state.eventId) { callback ->
            rule.setMediaDetailsBottomSheet(
                state = state,
                onShare = callback,
            )
            rule.clickOn(CommonStrings.action_share)
        }
    }

    @Test
    fun `clicking on Save invokes expected callback`() {
        val state = aMediaDetailsBottomSheetState()
        ensureCalledOnceWithParam(state.eventId) { callback ->
            rule.setMediaDetailsBottomSheet(
                state = state,
                onDownload = callback,
            )
            rule.clickOn(CommonStrings.action_save)
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on Remove invokes expected callback`() {
        val state = aMediaDetailsBottomSheetState()
        ensureCalledOnceWithParam(state.eventId) { callback ->
            rule.setMediaDetailsBottomSheet(
                state = state,
                onDelete = callback,
            )
            rule.onNodeWithText(rule.activity.getString(CommonStrings.action_remove)).assertExists()
            rule.clickOn(CommonStrings.action_remove)
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `Remove is not present if canDelete is false`() {
        val state = aMediaDetailsBottomSheetState(
            canDelete = false,
        )
        rule.setMediaDetailsBottomSheet(
            state = state,
        )
        rule.onNodeWithText(rule.activity.getString(CommonStrings.action_remove)).assertDoesNotExist()
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setMediaDetailsBottomSheet(
    state: MediaBottomSheetState.MediaDetailsBottomSheetState,
    onViewInTimeline: (EventId) -> Unit = EnsureNeverCalledWithParam(),
    onShare: (EventId) -> Unit = EnsureNeverCalledWithParam(),
    onDownload: (EventId) -> Unit = EnsureNeverCalledWithParam(),
    onDelete: (EventId) -> Unit = EnsureNeverCalledWithParam(),
    onDismiss: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        MediaDetailsBottomSheet(
            state = state,
            onViewInTimeline = onViewInTimeline,
            onShare = onShare,
            onDownload = onDownload,
            onDelete = onDelete,
            onDismiss = onDismiss,
        )
    }
}

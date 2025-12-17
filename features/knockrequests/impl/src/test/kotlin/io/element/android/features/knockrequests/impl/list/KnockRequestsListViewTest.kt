/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.list

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.knockrequests.impl.R
import io.element.android.features.knockrequests.impl.data.aKnockRequestPresentable
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KnockRequestsListViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on back invoke the expected callback`() {
        val eventsRecorder = EventsRecorder<KnockRequestsListEvents>(expectEvents = false)
        ensureCalledOnce {
            rule.setKnockRequestsListView(
                aKnockRequestsListState(
                    eventSink = eventsRecorder,
                ),
                onBackClick = it
            )
            rule.pressBack()
        }
    }

    @Test
    fun `clicking on accept emit the expected event`() {
        val eventsRecorder = EventsRecorder<KnockRequestsListEvents>()
        val knockRequest = aKnockRequestPresentable()
        rule.setKnockRequestsListView(
            aKnockRequestsListState(
                knockRequests = AsyncData.Success(persistentListOf(knockRequest)),
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_accept)
        eventsRecorder.assertSingle(KnockRequestsListEvents.Accept(knockRequest))
    }

    @Test
    fun `clicking on decline emit the expected event`() {
        val eventsRecorder = EventsRecorder<KnockRequestsListEvents>()
        val knockRequest = aKnockRequestPresentable()
        rule.setKnockRequestsListView(
            aKnockRequestsListState(
                knockRequests = AsyncData.Success(persistentListOf(knockRequest)),
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_decline)
        eventsRecorder.assertSingle(KnockRequestsListEvents.Decline(knockRequest))
    }

    @Test
    fun `clicking on decline and ban emit the expected event`() {
        val eventsRecorder = EventsRecorder<KnockRequestsListEvents>()
        val knockRequest = aKnockRequestPresentable()
        rule.setKnockRequestsListView(
            aKnockRequestsListState(
                knockRequests = AsyncData.Success(persistentListOf(knockRequest)),
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(R.string.screen_knock_requests_list_decline_and_ban_action_title)
        eventsRecorder.assertSingle(KnockRequestsListEvents.DeclineAndBan(knockRequest))
    }

    @Test
    fun `clicking on accept all emit the expected event`() {
        val eventsRecorder = EventsRecorder<KnockRequestsListEvents>()
        val knockRequests = persistentListOf(aKnockRequestPresentable(), aKnockRequestPresentable())
        rule.setKnockRequestsListView(
            aKnockRequestsListState(
                knockRequests = AsyncData.Success(knockRequests),
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(R.string.screen_knock_requests_list_accept_all_button_title)
        eventsRecorder.assertSingle(KnockRequestsListEvents.AcceptAll)
    }

    @Test
    fun `retry on async view retry emit the expected event`() {
        val eventsRecorder = EventsRecorder<KnockRequestsListEvents>()
        val knockRequests = persistentListOf(aKnockRequestPresentable(), aKnockRequestPresentable())
        rule.setKnockRequestsListView(
            aKnockRequestsListState(
                knockRequests = AsyncData.Success(knockRequests),
                asyncAction = AsyncAction.Failure(RuntimeException("Failed to accept all")),
                currentAction = KnockRequestsAction.AcceptAll,
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_retry)
        eventsRecorder.assertSingle(KnockRequestsListEvents.RetryCurrentAction)
    }

    @Test
    fun `canceling async view emit the expected event`() {
        val eventsRecorder = EventsRecorder<KnockRequestsListEvents>()
        val knockRequests = persistentListOf(aKnockRequestPresentable(), aKnockRequestPresentable())
        rule.setKnockRequestsListView(
            aKnockRequestsListState(
                knockRequests = AsyncData.Success(knockRequests),
                asyncAction = AsyncAction.Failure(RuntimeException("Failed to accept all")),
                currentAction = KnockRequestsAction.AcceptAll,
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(KnockRequestsListEvents.ResetCurrentAction)
    }

    @Test
    fun `confirming async view emit the expected event`() {
        val eventsRecorder = EventsRecorder<KnockRequestsListEvents>()
        val knockRequests = persistentListOf(aKnockRequestPresentable(), aKnockRequestPresentable())
        rule.setKnockRequestsListView(
            aKnockRequestsListState(
                knockRequests = AsyncData.Success(knockRequests),
                asyncAction = AsyncAction.ConfirmingNoParams,
                currentAction = KnockRequestsAction.AcceptAll,
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(R.string.screen_knock_requests_list_accept_all_alert_confirm_button_title)
        eventsRecorder.assertSingle(KnockRequestsListEvents.ConfirmCurrentAction)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setKnockRequestsListView(
    state: KnockRequestsListState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        KnockRequestsListView(
            state = state,
            onBackClick = onBackClick,
        )
    }
}

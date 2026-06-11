/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.messages.impl.link

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.wysiwyg.link.Link
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LinkViewTest {
    @Test
    fun `clicking on cancel emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LinkEvent>()
        setLinkView(
            aLinkState(
                linkClick = ConfirmingLinkClick(aLink),
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(
            LinkEvent.Cancel
        )
    }

    @Test
    fun `clicking on continue emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LinkEvent>()
        setLinkView(
            aLinkState(
                linkClick = ConfirmingLinkClick(aLink),
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_continue)
        eventsRecorder.assertSingle(
            LinkEvent.Confirm
        )
    }

    @Test
    fun `success state invokes the callback and emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LinkEvent>()
        ensureCalledOnceWithParam(aLink) { callback ->
            setLinkView(
                aLinkState(
                    linkClick = AsyncAction.Success(aLink),
                    eventSink = eventsRecorder,
                ),
                onLinkValid = callback,
            )
        }
        eventsRecorder.assertSingle(
            LinkEvent.Cancel
        )
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setLinkView(
    state: LinkState,
    onLinkValid: (Link) -> Unit = EnsureNeverCalledWithParam(),
) {
    setContent {
        LinkView(
            state = state,
            onLinkValid = onLinkValid,
        )
    }
}

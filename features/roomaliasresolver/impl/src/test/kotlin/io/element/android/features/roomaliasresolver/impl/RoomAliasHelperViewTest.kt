/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.roomaliasresolver.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.room.alias.ResolvedRoomAlias
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.pressBack
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomAliasHelperViewTest {
    @Test
    fun `clicking on back invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomAliasResolverEvents>(expectEvents = false)
        ensureCalledOnce {
            setRoomAliasResolverView(
                aRoomAliasResolverState(
                    eventSink = eventsRecorder,
                ),
                onBackClick = it
            )
            pressBack()
        }
    }

    @Test
    fun `clicking on Retry emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomAliasResolverEvents>()
        setRoomAliasResolverView(
            aRoomAliasResolverState(
                resolveState = AsyncData.Failure(Exception("Error")),
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_retry)
        eventsRecorder.assertSingle(RoomAliasResolverEvents.Retry)
    }

    @Test
    fun `success state invokes the expected Callback`() = runAndroidComposeUiTest {
        val result = aResolvedRoomAlias()
        val eventsRecorder = EventsRecorder<RoomAliasResolverEvents>(expectEvents = false)
        ensureCalledOnceWithParam(result) {
            setRoomAliasResolverView(
                aRoomAliasResolverState(
                    resolveState = AsyncData.Success(result),
                    eventSink = eventsRecorder,
                ),
                onAliasResolved = it,
            )
        }
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setRoomAliasResolverView(
    state: RoomAliasResolverState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
    onAliasResolved: (ResolvedRoomAlias) -> Unit = EnsureNeverCalledWithParam(),
) {
    setContent {
        RoomAliasResolverView(
            state = state,
            onBackClick = onBackClick,
            onSuccess = onAliasResolved,
        )
    }
}

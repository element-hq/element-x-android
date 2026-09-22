/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.callnative.impl

import android.Manifest
import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import io.element.android.call.test.FakeElementCallController
import io.element.android.features.call.api.CallData
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.tests.testutils.robolectric.RobolectricTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultNativeCallEntryPointTest : RobolectricTest() {
    @Test
    fun `starting a call answers the microphone permission the app already holds`() = runTest {
        grantMicrophonePermission()
        val controller = FakeElementCallController()
        val entryPoint = createEntryPoint(controller)

        entryPoint.startCall(A_CALL_DATA)
        runCurrent()

        assertThat(controller.startedCalls.map { it.roomId.value }).containsExactly(A_ROOM_ID.value)
        // Without this the call parks at RequestingPermission forever, and because a call is then
        // "running" every later attempt is refused with nothing on screen to hang it up.
        assertThat(controller.microphonePermissionAnswers).containsExactly(true)
    }

    @Test
    fun `a call with no UI is not maximized`() = runTest {
        grantMicrophonePermission()
        val controller = FakeElementCallController()
        val entryPoint = createEntryPoint(controller)

        entryPoint.startCall(A_CALL_DATA)
        runCurrent()

        // The snapshot assumes a call is on screen. While there is no call UI it never is, and the
        // component holds a proximity wake lock for a maximized audio call - which blanks the display
        // whenever a hand goes near the top of the phone, including reaching for the shade.
        assertThat(controller.maximizedCalls).containsExactly(false)
    }

    @Test
    fun `no call is started when the microphone permission is missing`() = runTest {
        shadowOf(RuntimeEnvironment.getApplication()).denyPermissions(Manifest.permission.RECORD_AUDIO)
        val controller = FakeElementCallController()
        val entryPoint = createEntryPoint(controller)

        entryPoint.startCall(A_CALL_DATA)
        runCurrent()

        // Nothing to ask with, so nothing is started: a call left unanswered would wedge the
        // controller for the rest of the session.
        assertThat(controller.startedCalls).isEmpty()
    }

    @Test
    fun `nothing happens for a session that cannot be restored`() = runTest {
        grantMicrophonePermission()
        val controller = FakeElementCallController()
        val entryPoint = createEntryPoint(controller, sessionCanBeRestored = false)

        entryPoint.startCall(A_CALL_DATA)
        runCurrent()

        assertThat(controller.startedCalls).isEmpty()
    }

    private fun grantMicrophonePermission() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.RECORD_AUDIO)
    }

    private fun TestScope.createEntryPoint(
        controller: FakeElementCallController,
        sessionCanBeRestored: Boolean = true,
    ) = DefaultNativeCallEntryPoint(
        context = InstrumentationRegistry.getInstrumentation().targetContext as Application,
        controllers = FakeElementCallControllers(controller.takeIf { sessionCanBeRestored }),
        appCoroutineScope = this,
    )

    private companion object {
        val A_CALL_DATA = CallData(sessionId = A_SESSION_ID, roomId = A_ROOM_ID, isAudioCall = true)
    }
}

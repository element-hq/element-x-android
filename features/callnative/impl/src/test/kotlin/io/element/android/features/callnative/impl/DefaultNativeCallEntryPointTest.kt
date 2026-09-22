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
        // The call screen normally answers this, but it is not composed when a call is answered from
        // a notification with the app in the background - and a call waiting on a permission nobody
        // is there to grant sits unanswered and refuses every later attempt.
        assertThat(controller.microphonePermissionAnswers).containsExactly(true)
    }

    @Test
    fun `a call the user placed opens on the call screen, not in the minimized bar`() = runTest {
        grantMicrophonePermission()
        val controller = FakeElementCallController()
        val entryPoint = createEntryPoint(controller)

        entryPoint.startCall(A_CALL_DATA)
        runCurrent()

        // A snapshot starts maximized, which is what the user asked for by pressing call. Saying
        // otherwise here is what a UI-less build had to do, and it would now dock a freshly placed
        // call into the bar.
        assertThat(controller.maximizedCalls).isEmpty()
    }

    @Test
    fun `the call still starts without the microphone permission, so the call screen can ask`() = runTest {
        shadowOf(RuntimeEnvironment.getApplication()).denyPermissions(Manifest.permission.RECORD_AUDIO)
        val controller = FakeElementCallController()
        val entryPoint = createEntryPoint(controller)

        entryPoint.startCall(A_CALL_DATA)
        runCurrent()

        assertThat(controller.startedCalls).hasSize(1)
        // Nothing to report, and nothing to report it from: only an Activity can ask, and the call
        // screen does exactly that once it appears.
        assertThat(controller.microphonePermissionAnswers).isEmpty()
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

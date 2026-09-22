/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.callnative.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.call.api.ElementCallData
import io.element.android.call.api.rtc.id.RoomId
import io.element.android.features.call.api.CurrentCall
import io.element.android.features.call.test.FakeCurrentCallTracker
import io.element.android.features.call.test.FakeRingingCallTracker
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.services.appnavstate.test.FakeAppForegroundStateService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ElementXCallLifecycleListenerTest {
    @Test
    fun `a started call is reported as the current call, and cleared when it ends`() = runTest {
        val currentCallTracker = FakeCurrentCallTracker()
        val listener = createListener(currentCallTracker = currentCallTracker)

        listener.onCallStarted(A_CALL)
        listener.onCallEnded(A_CALL)

        assertThat(currentCallTracker.calls).containsExactly(
            CurrentCall.RoomCall(A_ROOM_ID),
            CurrentCall.None,
        ).inOrder()
    }

    @Test
    fun `joining a call stops the ring`() = runTest {
        val ringingCallTracker = FakeRingingCallTracker()
        val listener = createListener(ringingCallTracker = ringingCallTracker)

        listener.onCallJoined(A_CALL)
        runCurrent()

        assertThat(ringingCallTracker.joined.map { it.roomId }).containsExactly(A_ROOM_ID)
        assertThat(ringingCallTracker.ended).isEmpty()
    }

    @Test
    fun `ending a call tells the ringing side, and survives a call that never had data`() = runTest {
        val ringingCallTracker = FakeRingingCallTracker()
        val currentCallTracker = FakeCurrentCallTracker()
        val listener = createListener(
            currentCallTracker = currentCallTracker,
            ringingCallTracker = ringingCallTracker,
        )

        listener.onCallEnded(null)
        runCurrent()

        // The current call is always cleared; only the ring needs to know which call it was.
        assertThat(currentCallTracker.calls).containsExactly(CurrentCall.None)
        assertThat(ringingCallTracker.ended).isEmpty()
    }

    private fun TestScope.createListener(
        currentCallTracker: FakeCurrentCallTracker = FakeCurrentCallTracker(),
        ringingCallTracker: FakeRingingCallTracker = FakeRingingCallTracker(),
    ) = ElementXCallLifecycleListener(
        sessionId = A_SESSION_ID,
        currentCallTracker = currentCallTracker,
        ringingCallTracker = ringingCallTracker,
        sessionCoroutineScope = this,
        appForegroundStateService = FakeAppForegroundStateService(),
    )

    private companion object {
        val A_CALL = ElementCallData(roomId = RoomId(A_ROOM_ID.value), isAudioCall = true)
    }
}

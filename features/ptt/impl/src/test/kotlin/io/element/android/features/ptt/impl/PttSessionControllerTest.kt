/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.features.ptt.api.PttChannelConfig
import io.element.android.features.ptt.api.PttTransport
import io.element.android.features.ptt.api.PttTransportFactory
import io.element.android.features.ptt.api.PttTransportType
import io.element.android.features.ptt.api.initialPttSessionState
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PttSessionControllerTest {
    private val config = PttChannelConfig(A_SESSION_ID, A_ROOM_ID, PttTransportType.Mumble)
    private val outputChanges = mutableListOf<Boolean>()
    private val transport = mockk<PttTransport>(relaxed = true) {
        every { state } returns MutableStateFlow(initialPttSessionState(PttTransportType.Mumble))
        coEvery { setAudioOutputEnabled(any()) } coAnswers { outputChanges.add(firstArg()) }
    }

    private fun TestScope.controller(): PttSessionController = PttSessionController(
        transportFactory = mockk<PttTransportFactory> { every { create(any()) } returns transport },
        tones = mockk(relaxed = true),
        coroutineScope = backgroundScope,
    )

    @Test
    fun `joining enables playback and covert toggles mute then restore it`() = runTest {
        val controller = controller()
        controller.startSession(config)
        runCurrent()
        assertThat(outputChanges).containsExactly(true)
        controller.setCovert(true)
        runCurrent()
        controller.setCovert(false)
        runCurrent()
        assertThat(outputChanges).containsExactly(true, false, true).inOrder()
    }

    @Test
    fun `joining while covert never enables playback`() = runTest {
        val controller = controller()
        controller.setCovert(true)
        controller.startSession(config)
        runCurrent()
        assertThat(outputChanges).containsExactly(false)
    }

    @Test
    fun `covert enabled during connection prevents playback after joining`() = runTest {
        val joined = CompletableDeferred<Unit>()
        coEvery { transport.join(any()) } coAnswers { joined.await() }
        val controller = controller()
        controller.startSession(config)
        runCurrent()
        controller.setCovert(true)
        joined.complete(Unit)
        runCurrent()
        assertThat(outputChanges).isNotEmpty()
        assertThat(outputChanges).doesNotContain(true)
        controller.setCovert(false)
        runCurrent()
        assertThat(outputChanges.last()).isTrue()
    }
}

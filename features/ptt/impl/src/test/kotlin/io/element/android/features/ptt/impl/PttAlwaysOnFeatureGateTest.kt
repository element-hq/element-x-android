/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package io.element.android.features.ptt.impl

import io.element.android.features.ptt.api.PttEnabledSource
import io.element.android.features.ptt.api.PttSessionState
import io.element.android.features.ptt.api.PttTransportType
import io.element.android.features.ptt.api.initialPttSessionState
import io.element.android.features.ptt.impl.services.PttSessionHostService
import io.element.android.features.ptt.test.FakePttFeatureAvailability
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.sessionstorage.api.SessionData
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.tests.testutils.robolectric.RobolectricTest
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PttAlwaysOnFeatureGateTest : RobolectricTest() {
    @Test
    fun `old room settings cannot start PTT until the flag is enabled and disabling stops it`() = runTest {
        val availability = FakePttFeatureAvailability(false)
        val live = MutableStateFlow<PttSessionState?>(null)
        val session = mockk<SessionData> { every { userId } returns A_SESSION_ID.value }
        val sessions = mockk<SessionStore> { every { sessionsFlow() } returns flowOf(listOf(session)) }
        val rooms = mockk<PttEnabledSource> { every { enabledRoomIdsFlow(A_SESSION_ID) } returns flowOf(setOf(A_ROOM_ID)) }
        val controller = mockk<PttSessionController> { every { sessionState } returns live }
        val settings = mockk<PttLocalSettings> { every { server(A_SESSION_ID) } returns PttServerSettings("mumble.example.org") }
        mockkObject(PttSessionHostService.Companion)
        try {
            every { PttSessionHostService.start(any(), any()) } just Runs
            every { PttSessionHostService.hangup(any()) } just Runs
            PttAlwaysOnController(
                context = mockk(relaxed = true),
                coroutineScope = backgroundScope,
                sessionStore = sessions,
                pttEnabledSource = rooms,
                sessionController = controller,
                settings = settings,
                availability = availability,
                factories = mapOf(PttTransportType.Mumble to mockk()),
            ).start()
            runCurrent()
            verify(exactly = 0) { PttSessionHostService.start(any(), any()) }
            availability.isAvailable.value = true
            runCurrent()
            verify(exactly = 1) { PttSessionHostService.start(any(), any()) }
            live.value = initialPttSessionState(PttTransportType.Mumble)
            runCurrent()
            availability.isAvailable.value = false
            runCurrent()
            verify(exactly = 1) { PttSessionHostService.hangup(any()) }
            live.value = null
            runCurrent()
            verify(exactly = 1) { PttSessionHostService.start(any(), any()) }
        } finally {
            unmockkObject(PttSessionHostService.Companion)
        }
    }
}

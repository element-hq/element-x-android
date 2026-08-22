/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.feral.connection

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.androidutils.json.DefaultJsonProvider
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.push.test.push.FakeFetchPushForegroundServiceManager
import io.element.android.libraries.push.test.test.FakePushHandler
import io.element.android.libraries.pushproviders.api.PushData
import io.element.android.libraries.pushproviders.feral.FakeFeralPushStore
import io.element.android.libraries.pushproviders.feral.aFeralPushRegistration
import io.element.android.libraries.pushproviders.unifiedpush.UnifiedPushParser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class FeralPushConnectionTest {
    private val registration = aFeralPushRegistration(A_SESSION_ID, topic = "uptopic", clientSecret = "secret")
    private val notification = """{"notification":{"event_id":"${'$'}anEvent","room_id":"!aRoom:server"}}"""

    private class Fixture(
        val factory: FakeFeralPushSocketFactory = FakeFeralPushSocketFactory(),
        val store: FakeFeralPushStore,
        val handled: MutableList<PushData> = mutableListOf(),
        val connectedChanges: MutableList<Boolean> = mutableListOf(),
    )

    private fun TestScope.createFixture(lastMessageId: String? = null): Pair<Fixture, FeralPushConnection> {
        val store = FakeFeralPushStore(listOf(registration.copy(lastMessageId = lastMessageId)))
        val fixture = Fixture(store = store)
        val connection = FeralPushConnection(
            registration = registration,
            socketFactory = fixture.factory,
            frameParser = FeralPushFrameParser(DefaultJsonProvider()),
            messageHandler = FeralPushMessageHandler(
                parser = UnifiedPushParser(DefaultJsonProvider()),
                pushHandler = FakePushHandler(handleResult = { data, _ -> fixture.handled += data; true }),
                feralPushStore = store,
                fetchPushForegroundServiceManager = FakeFetchPushForegroundServiceManager(),
            ),
            lastMessageIdProvider = { store.get(A_SESSION_ID)?.lastMessageId },
            onConnectedChanged = { fixture.connectedChanges += it },
            backoff = ReconnectBackoff(initial = 1.seconds, max = 60.seconds, jitterRatio = 0.0),
            now = { testScheduler.currentTime },
            watchdogTimeout = 120.seconds,
            healthyAfter = 60.seconds,
        )
        return fixture to connection
    }

    private fun message(id: String) = """{"id":"$id","time":1,"event":"message","topic":"uptopic","message":${'"'}${notification.replace("\"", "\\\"")}${'"'}}"""

    private fun keepalive(id: String) = """{"id":"$id","time":1,"event":"keepalive","topic":"uptopic"}"""

    @Test
    fun `connects to the topic stream, reports the state and handles message frames only`() = runTest {
        val (fixture, connection) = createFixture()
        backgroundScope.launch { connection.run() }
        runCurrent()
        assertThat(fixture.factory.sockets).hasSize(1)
        assertThat(fixture.factory.last.url).isEqualTo("wss://ntfy.feralisme.fr/uptopic/ws")

        fixture.factory.last.listener.onOpen()
        runCurrent()
        assertThat(fixture.connectedChanges).containsExactly(true)

        fixture.factory.last.listener.onFrame("""{"id":"o","event":"open","topic":"uptopic"}""")
        fixture.factory.last.listener.onFrame(keepalive("k1"))
        fixture.factory.last.listener.onFrame("garbage")
        runCurrent()
        assertThat(fixture.handled).isEmpty()

        fixture.factory.last.listener.onFrame(message("m1"))
        runCurrent()
        assertThat(fixture.handled).hasSize(1)
        assertThat(fixture.store.get(A_SESSION_ID)!!.lastMessageId).isEqualTo("m1")
        assertThat(fixture.factory.last.isCancelled).isFalse()
    }

    @Test
    fun `reconnects with exponential backoff and resumes from the last message id`() = runTest {
        val (fixture, connection) = createFixture(lastMessageId = "m0")
        backgroundScope.launch { connection.run() }
        runCurrent()
        assertThat(fixture.factory.last.url).isEqualTo("wss://ntfy.feralisme.fr/uptopic/ws?since=m0")

        fixture.factory.last.listener.onFailure(IllegalStateException("down"))
        runCurrent()
        assertThat(fixture.factory.sockets).hasSize(1)
        assertThat(fixture.factory.last.isCancelled).isTrue()
        // No connected=false: the socket never opened.
        assertThat(fixture.connectedChanges).isEmpty()

        advanceTimeBy(999)
        runCurrent()
        assertThat(fixture.factory.sockets).hasSize(1)
        advanceTimeBy(1)
        runCurrent()
        assertThat(fixture.factory.sockets).hasSize(2)

        fixture.factory.last.listener.onClosed("1006 abnormal")
        runCurrent()
        advanceTimeBy(1999)
        runCurrent()
        assertThat(fixture.factory.sockets).hasSize(2)
        advanceTimeBy(1)
        runCurrent()
        assertThat(fixture.factory.sockets).hasSize(3)

        fixture.factory.last.listener.onOpen()
        fixture.factory.last.listener.onFrame(message("m7"))
        fixture.factory.last.listener.onFailure(IllegalStateException("down again"))
        runCurrent()
        assertThat(fixture.connectedChanges).containsExactly(true, false)
        advanceTimeBy(4000)
        runCurrent()
        assertThat(fixture.factory.sockets).hasSize(4)
        assertThat(fixture.factory.last.url).isEqualTo("wss://ntfy.feralisme.fr/uptopic/ws?since=m7")
    }

    @Test
    fun `the watchdog closes a silent connection and reconnects`() = runTest {
        val (fixture, connection) = createFixture()
        backgroundScope.launch { connection.run() }
        runCurrent()
        fixture.factory.last.listener.onOpen()
        runCurrent()

        // Keepalives keep the watchdog quiet.
        advanceTimeBy(100.seconds)
        fixture.factory.last.listener.onFrame(keepalive("k1"))
        runCurrent()
        advanceTimeBy(100.seconds)
        runCurrent()
        assertThat(fixture.factory.sockets).hasSize(1)
        assertThat(fixture.factory.last.isCancelled).isFalse()

        // 120 s without any frame: closed and reconnected after the backoff.
        advanceTimeBy(20.seconds)
        runCurrent()
        assertThat(fixture.factory.sockets[0].isCancelled).isTrue()
        assertThat(fixture.connectedChanges).containsExactly(true, false)
        advanceTimeBy(1.seconds)
        runCurrent()
        assertThat(fixture.factory.sockets).hasSize(2)
    }

    @Test
    fun `the backoff is reset after a healthy connection`() = runTest {
        val (fixture, connection) = createFixture()
        backgroundScope.launch { connection.run() }
        runCurrent()
        fixture.factory.last.listener.onFailure(IllegalStateException("1"))
        advanceTimeBy(1.seconds)
        runCurrent()
        fixture.factory.last.listener.onFailure(IllegalStateException("2"))
        advanceTimeBy(2.seconds)
        runCurrent()
        assertThat(fixture.factory.sockets).hasSize(3)

        // Stays open for more than 60 s (with keepalives): healthy.
        fixture.factory.last.listener.onOpen()
        runCurrent()
        advanceTimeBy(45.seconds)
        fixture.factory.last.listener.onFrame(keepalive("k1"))
        runCurrent()
        advanceTimeBy(45.seconds)
        fixture.factory.last.listener.onFrame(keepalive("k2"))
        runCurrent()
        fixture.factory.last.listener.onFailure(IllegalStateException("3"))
        runCurrent()

        // Back to the initial 1 s delay instead of 4 s.
        advanceTimeBy(1.seconds)
        runCurrent()
        assertThat(fixture.factory.sockets).hasSize(4)
    }

    @Test
    fun `a network becoming available cuts the backoff short`() = runTest {
        val (fixture, connection) = createFixture()
        backgroundScope.launch { connection.run() }
        runCurrent()
        repeat(3) {
            fixture.factory.last.listener.onFailure(IllegalStateException("down"))
            advanceTimeBy(60.seconds)
            runCurrent()
        }
        assertThat(fixture.factory.sockets).hasSize(4)
        // Now waiting 8 s.
        fixture.factory.last.listener.onFailure(IllegalStateException("down"))
        advanceTimeBy(1.seconds)
        runCurrent()
        assertThat(fixture.factory.sockets).hasSize(4)
        connection.onNetworkAvailable()
        runCurrent()
        assertThat(fixture.factory.sockets).hasSize(5)
    }
}

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
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.push.test.push.FakeFetchPushForegroundServiceManager
import io.element.android.libraries.push.test.test.FakePushHandler
import io.element.android.libraries.pushproviders.api.PushData
import io.element.android.libraries.pushproviders.feral.FakeFeralPushStore
import io.element.android.libraries.pushproviders.feral.aFeralPushRegistration
import io.element.android.libraries.pushproviders.unifiedpush.UnifiedPushParser
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.io.encoding.Base64

class FeralPushMessageHandlerTest {
    private val registration = aFeralPushRegistration(A_SESSION_ID, clientSecret = "secret")
    private val notification = """{"notification":{"event_id":"${'$'}anEvent","room_id":"!aRoom:server","counts":{"unread":3}}}"""

    @Test
    fun `a valid notification is handed to the push handler with the session secret and the cursor is saved`() = runTest {
        val handled = mutableListOf<Pair<PushData, String>>()
        val starts = mutableListOf<Boolean>()
        val stops = mutableListOf<Boolean>()
        val store = FakeFeralPushStore(listOf(registration))
        val handler = FeralPushMessageHandler(
            parser = UnifiedPushParser(DefaultJsonProvider()),
            pushHandler = FakePushHandler(handleResult = { data, info -> handled += data to info; true }),
            feralPushStore = store,
            fetchPushForegroundServiceManager = FakeFetchPushForegroundServiceManager(lock = { starts += true; true }, unlock = { stops += true; true }),
        )
        handler.handle(registration, FeralPushFrame(id = "m1", event = "message", message = notification))
        assertThat(handled).containsExactly(
            PushData(eventId = EventId("\$anEvent"), roomId = RoomId("!aRoom:server"), unread = 3, clientSecret = "secret") to "Feral",
        )
        assertThat(starts).hasSize(1)
        assertThat(stops).isEmpty()
        assertThat(store.get(A_SESSION_ID)!!.lastMessageId).isEqualTo("m1")
    }

    @Test
    fun `a base64 body is decoded before parsing`() = runTest {
        val handled = mutableListOf<PushData>()
        val handler = FeralPushMessageHandler(
            parser = UnifiedPushParser(DefaultJsonProvider()),
            pushHandler = FakePushHandler(handleResult = { data, _ -> handled += data; true }),
            feralPushStore = FakeFeralPushStore(listOf(registration)),
            fetchPushForegroundServiceManager = FakeFetchPushForegroundServiceManager(),
        )
        val encoded = Base64.Default.encode(notification.encodeToByteArray())
        handler.handle(registration, FeralPushFrame(id = "m2", event = "message", message = encoded, encoding = "base64"))
        assertThat(handled.single().eventId).isEqualTo(EventId("\$anEvent"))
    }

    @Test
    fun `an invalid body is reported as invalid and the wakelock released`() = runTest {
        val invalid = mutableListOf<Pair<String, String>>()
        val stops = mutableListOf<Boolean>()
        val store = FakeFeralPushStore(listOf(registration))
        val handler = FeralPushMessageHandler(
            parser = UnifiedPushParser(DefaultJsonProvider()),
            pushHandler = FakePushHandler(handleInvalidResult = { info, data -> invalid += info to data }),
            feralPushStore = store,
            fetchPushForegroundServiceManager = FakeFetchPushForegroundServiceManager(unlock = { stops += true; true }),
        )
        handler.handle(registration, FeralPushFrame(id = "m3", event = "message", message = "hello"))
        assertThat(invalid).containsExactly("Feral" to "hello")
        assertThat(stops).hasSize(1)
        // The cursor still moves forward: the message was consumed.
        assertThat(store.get(A_SESSION_ID)!!.lastMessageId).isEqualTo("m3")
    }

    @Test
    fun `a push that is not handled releases the wakelock`() = runTest {
        val stops = mutableListOf<Boolean>()
        val handler = FeralPushMessageHandler(
            parser = UnifiedPushParser(DefaultJsonProvider()),
            pushHandler = FakePushHandler(handleResult = { _, _ -> false }),
            feralPushStore = FakeFeralPushStore(listOf(registration)),
            fetchPushForegroundServiceManager = FakeFetchPushForegroundServiceManager(unlock = { stops += true; true }),
        )
        handler.handle(registration, FeralPushFrame(id = "m4", event = "message", message = notification))
        assertThat(stops).hasSize(1)
    }
}

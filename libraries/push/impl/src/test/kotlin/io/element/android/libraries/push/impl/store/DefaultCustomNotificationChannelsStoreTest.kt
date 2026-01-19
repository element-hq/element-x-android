/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.store

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import io.element.android.libraries.sessionstorage.test.observer.NoOpSessionObserver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DefaultCustomNotificationChannelsStoreTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val roomId1 = RoomId("!room1:example.com")
    private val roomId2 = RoomId("!room2:example.com")
    private val sessionId = SessionId("@user:example.com")

    @Test
    fun `initially has no custom channels`() = runTest {
        val store = createStore()

        val roomIds = store.roomIdsWithCustomChannel().first()

        assertThat(roomIds).isEmpty()
    }

    @Test
    fun `addCustomChannel adds room to store`() = runTest {
        val store = createStore()

        store.addCustomChannel(roomId1)
        val roomIds = store.roomIdsWithCustomChannel().first()

        assertThat(roomIds).containsExactly(roomId1)
    }

    @Test
    fun `addCustomChannel adds multiple rooms`() = runTest {
        val store = createStore()

        store.addCustomChannel(roomId1)
        store.addCustomChannel(roomId2)
        val roomIds = store.roomIdsWithCustomChannel().first()

        assertThat(roomIds).containsExactly(roomId1, roomId2)
    }

    @Test
    fun `hasCustomChannel returns true for added room`() = runTest {
        val store = createStore()
        store.addCustomChannel(roomId1)

        assertThat(store.hasCustomChannel(roomId1)).isTrue()
        assertThat(store.hasCustomChannel(roomId2)).isFalse()
    }

    @Test
    fun `removeCustomChannel removes room from store`() = runTest {
        val store = createStore()
        store.addCustomChannel(roomId1)
        store.addCustomChannel(roomId2)

        store.removeCustomChannel(roomId1)
        val roomIds = store.roomIdsWithCustomChannel().first()

        assertThat(roomIds).containsExactly(roomId2)
    }

    @Test
    fun `clear removes all rooms from store`() = runTest {
        val store = createStore()
        store.addCustomChannel(roomId1)
        store.addCustomChannel(roomId2)

        store.clear()
        val roomIds = store.roomIdsWithCustomChannel().first()

        assertThat(roomIds).isEmpty()
    }

    private fun TestScope.createStore(
        sessionObserver: SessionObserver = NoOpSessionObserver(),
    ): DefaultCustomNotificationChannelsStore {
        return DefaultCustomNotificationChannelsStore(
            context = RuntimeEnvironment.getApplication(),
            sessionId = sessionId,
            sessionCoroutineScope = backgroundScope,
            sessionObserver = sessionObserver,
        )
    }
}

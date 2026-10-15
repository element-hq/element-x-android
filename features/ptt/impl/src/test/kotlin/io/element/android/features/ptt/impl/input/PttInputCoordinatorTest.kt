/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl.input

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.element.android.features.ptt.api.PttInputSource
import io.element.android.features.ptt.api.PttInputSourceFactory
import io.element.android.features.ptt.api.PttInputSourceId
import io.element.android.features.ptt.test.FakePttSessionManager
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.mockk.mockk
import org.junit.Test

class PttInputCoordinatorTest {
    @Test
    fun `start creates and starts available sources, stop releases them`() {
        val source = FakePttInputSource()
        val factory = FakePttInputSourceFactory(available = true, source = source)
        val coordinator = createCoordinator(mapOf(factory.id to factory))

        coordinator.start()
        assertThat(factory.createCount).isEqualTo(1)
        assertThat(source.started).isTrue()

        coordinator.stop()
        assertThat(source.stopped).isTrue()
    }

    @Test
    fun `an unavailable source is never created`() {
        val source = FakePttInputSource()
        val factory = FakePttInputSourceFactory(available = false, source = source)
        val coordinator = createCoordinator(mapOf(factory.id to factory))

        coordinator.start()

        assertThat(factory.createCount).isEqualTo(0)
        assertThat(source.started).isFalse()
    }

    @Test
    fun `start is idempotent`() {
        val source = FakePttInputSource()
        val factory = FakePttInputSourceFactory(available = true, source = source)
        val coordinator = createCoordinator(mapOf(factory.id to factory))

        coordinator.start()
        coordinator.start()

        assertThat(factory.createCount).isEqualTo(1)
    }

    @Test
    fun `a source press then release drives transmit once each`() {
        val pressRecorder = lambdaRecorder<Unit> { }
        val releaseRecorder = lambdaRecorder<Unit> { }
        val source = FakePttInputSource()
        val coordinator = createCoordinator(
            factories = mapOf(source.id to FakePttInputSourceFactory(available = true, source = source)),
            sessionManager = FakePttSessionManager(
                pressToTalkLambda = pressRecorder,
                releaseToTalkLambda = releaseRecorder,
            ),
        )
        coordinator.start()

        source.pressDown()
        source.pressUp()

        pressRecorder.assertions().isCalledOnce()
        releaseRecorder.assertions().isCalledOnce()
    }

    @Test
    fun `duplicate press and release are guarded`() {
        val pressRecorder = lambdaRecorder<Unit> { }
        val releaseRecorder = lambdaRecorder<Unit> { }
        val source = FakePttInputSource()
        val coordinator = createCoordinator(
            factories = mapOf(source.id to FakePttInputSourceFactory(available = true, source = source)),
            sessionManager = FakePttSessionManager(
                pressToTalkLambda = pressRecorder,
                releaseToTalkLambda = releaseRecorder,
            ),
        )
        coordinator.start()

        source.pressDown()
        source.pressDown() // duplicate down: guarded
        source.pressUp()
        source.pressUp() // duplicate up: guarded

        pressRecorder.assertions().isCalledOnce()
        releaseRecorder.assertions().isCalledOnce()
    }

    @Test
    fun `refresh starts a source that became available after start`() {
        val source = FakePttInputSource()
        val factory = FakePttInputSourceFactory(available = false, source = source)
        val coordinator = createCoordinator(mapOf(factory.id to factory))

        coordinator.start() // unavailable → not created yet
        assertThat(source.started).isFalse()

        factory.available = true // e.g. the user granted Bluetooth permission
        coordinator.refresh()

        assertThat(source.started).isTrue()
        assertThat(factory.createCount).isEqualTo(1)
    }

    @Test
    fun `refresh is a no-op when the session has not started`() {
        val source = FakePttInputSource()
        val factory = FakePttInputSourceFactory(available = true, source = source)
        val coordinator = createCoordinator(mapOf(factory.id to factory))

        coordinator.refresh()

        assertThat(source.started).isFalse()
    }

    @Test
    fun `Bluetooth setup retry releases a held floor and reconnects only Bluetooth`() {
        val releaseRecorder = lambdaRecorder<Unit> { }
        val bluetooth = FakePttInputSource(PttInputSourceId.BleButton)
        val media = FakePttInputSource()
        val bluetoothFactory = FakePttInputSourceFactory(true, bluetooth, bluetooth.id)
        val mediaFactory = FakePttInputSourceFactory(true, media)
        val coordinator = createCoordinator(
            mapOf(bluetooth.id to bluetoothFactory, media.id to mediaFactory),
            FakePttSessionManager(releaseToTalkLambda = releaseRecorder),
        )
        coordinator.start()
        bluetooth.pressDown()
        coordinator.retryBluetoothButton()
        releaseRecorder.assertions().isCalledOnce()
        assertThat(bluetoothFactory.createCount).isEqualTo(2)
        assertThat(mediaFactory.createCount).isEqualTo(1)
        assertThat(bluetooth.stopped).isTrue()
        assertThat(media.stopped).isFalse()
    }

    private fun createCoordinator(
        factories: Map<PttInputSourceId, PttInputSourceFactory>,
        sessionManager: FakePttSessionManager = FakePttSessionManager(),
    ) = PttInputCoordinator(
        context = mockk(relaxed = true),
        factories = factories,
        sessionManager = sessionManager,
    )
}

private class FakePttInputSource(
    override val id: PttInputSourceId = PttInputSourceId.MediaButton,
) : PttInputSource {
    var started = false
    var stopped = false
    private var onPress: (() -> Unit)? = null
    private var onRelease: (() -> Unit)? = null

    override fun start(onPressToTalk: () -> Unit, onReleaseToTalk: () -> Unit) {
        started = true
        onPress = onPressToTalk
        onRelease = onReleaseToTalk
    }

    override fun stop() {
        stopped = true
    }

    fun pressDown() = onPress?.invoke()
    fun pressUp() = onRelease?.invoke()
}

private class FakePttInputSourceFactory(
    var available: Boolean,
    private val source: FakePttInputSource,
    override val id: PttInputSourceId = PttInputSourceId.MediaButton,
) : PttInputSourceFactory {
    var createCount = 0
    override fun isAvailable(context: Context) = available
    override fun create(context: Context): PttInputSource {
        createCount++
        return source
    }
}

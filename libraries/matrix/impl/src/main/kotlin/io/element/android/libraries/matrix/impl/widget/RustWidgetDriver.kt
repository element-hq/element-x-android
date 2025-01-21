/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.widget

import io.element.android.libraries.matrix.api.widget.MatrixWidgetDriver
import io.element.android.libraries.matrix.api.widget.MatrixWidgetSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.WidgetCapabilitiesProvider
import org.matrix.rustcomponents.sdk.makeWidgetDriver
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.coroutineContext

class RustWidgetDriver(
    widgetSettings: MatrixWidgetSettings,
    private val room: Room,
    private val widgetCapabilitiesProvider: WidgetCapabilitiesProvider,
) : MatrixWidgetDriver {
    // It's important to have extra capacity here to make sure we don't drop any messages
    override val incomingMessages = MutableSharedFlow<String>(extraBufferCapacity = 10)

    private val driverAndHandle = makeWidgetDriver(widgetSettings.toRustWidgetSettings())
    private var receiveMessageJob: Job? = null

    private var isRunning = AtomicBoolean(false)

    override val id: String = widgetSettings.id

    override suspend fun run() {
        // Don't run the driver if it's already running
        if (!isRunning.compareAndSet(false, true)) {
            return
        }

        val coroutineScope = CoroutineScope(coroutineContext)
        coroutineScope.launch {
            // This call will suspend the coroutine while the driver is running, so it needs to be launched separately
            driverAndHandle.driver.run(room, widgetCapabilitiesProvider)
        }
        receiveMessageJob = coroutineScope.launch(Dispatchers.IO) {
            try {
                while (isActive) {
                    driverAndHandle.handle.recv()?.let { incomingMessages.emit(it) }
                }
            } finally {
                driverAndHandle.handle.close()
            }
        }
    }

    override suspend fun send(message: String) {
        try {
            driverAndHandle.handle.send(message)
        } catch (e: IllegalStateException) {
            // The handle is closed, ignore
        }
    }

    override fun close() {
        receiveMessageJob?.cancel()
        driverAndHandle.driver.close()
    }
}

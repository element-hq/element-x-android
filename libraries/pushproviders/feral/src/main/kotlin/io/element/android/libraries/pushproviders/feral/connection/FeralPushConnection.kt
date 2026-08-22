/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.feral.connection

import dev.zacsweers.metro.Inject
import io.element.android.appconfig.FeralPushConfig
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.pushproviders.feral.FeralPushRegistration
import io.element.android.libraries.pushproviders.feral.FeralPushStore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val loggerTag = LoggerTag("FeralPushConnection", LoggerTag.PushLoggerTag)

/**
 * One WebSocket to `wss://<server>/<topic>/ws` for one session, kept alive forever:
 * - frames are parsed and `message` ones handed to [FeralPushMessageHandler];
 * - a watchdog closes the socket when no frame at all arrived for [watchdogTimeout];
 * - reconnections use [ReconnectBackoff], reset once a connection stayed up for [healthyAfter];
 * - [onNetworkAvailable] cuts a pending backoff short.
 *
 * Plain Kotlin (no Android): [run] suspends until cancelled, all time comes from [delay]/[now].
 */
class FeralPushConnection(
    private val registration: FeralPushRegistration,
    private val socketFactory: FeralPushSocketFactory,
    private val frameParser: FeralPushFrameParser,
    private val messageHandler: FeralPushMessageHandler,
    private val lastMessageIdProvider: suspend () -> String?,
    private val onConnectedChanged: (Boolean) -> Unit,
    private val backoff: ReconnectBackoff = ReconnectBackoff(),
    private val now: () -> Long = System::currentTimeMillis,
    private val watchdogTimeout: Duration = FeralPushConfig.WATCHDOG_TIMEOUT,
    private val healthyAfter: Duration = FeralPushConfig.HEALTHY_AFTER,
    private val serverUrl: String = FeralPushConfig.SERVER_URL,
) {
    private sealed interface SocketEvent {
        data object Open : SocketEvent
        data class Frame(val text: String) : SocketEvent
        data class Closed(val reason: String) : SocketEvent
        data class Failure(val throwable: Throwable) : SocketEvent
    }

    private val reconnectSignal = Channel<Unit>(Channel.CONFLATED)

    /** Wake a pending backoff wait so the reconnection happens now. */
    fun onNetworkAvailable() {
        reconnectSignal.trySend(Unit)
    }

    suspend fun run() {
        while (currentCoroutineContext().isActive) {
            val url = buildUrl(lastMessageIdProvider())
            val healthy = connectOnce(url)
            if (healthy) {
                backoff.reset()
            }
            val wait = backoff.nextDelay()
            Timber.tag(loggerTag.value).d("Reconnecting in $wait")
            withTimeoutOrNull(wait) { reconnectSignal.receive() }
        }
    }

    /** Returns true when the connection stayed open for at least [healthyAfter]. */
    private suspend fun connectOnce(url: String): Boolean = coroutineScope {
        val events = Channel<SocketEvent>(Channel.UNLIMITED)
        val socket = socketFactory.open(
            url = url,
            listener = object : FeralPushSocketListener {
                override fun onOpen() {
                    events.trySend(SocketEvent.Open)
                }

                override fun onFrame(text: String) {
                    events.trySend(SocketEvent.Frame(text))
                }

                override fun onClosed(reason: String) {
                    events.trySend(SocketEvent.Closed(reason))
                }

                override fun onFailure(throwable: Throwable) {
                    events.trySend(SocketEvent.Failure(throwable))
                }
            }
        )
        var openedAt: Long? = null
        var healthy = false
        try {
            while (true) {
                val event = withTimeoutOrNull(watchdogTimeout) { events.receive() }
                if (event == null) {
                    Timber.tag(loggerTag.value).w("No frame for $watchdogTimeout, closing the socket")
                    break
                }
                when (event) {
                    SocketEvent.Open -> {
                        Timber.tag(loggerTag.value).i("Connected to the Feral server")
                        openedAt = now()
                        onConnectedChanged(true)
                    }
                    is SocketEvent.Frame -> {
                        val openedSince = openedAt?.let { (now() - it).milliseconds }
                        if (openedSince != null && openedSince >= healthyAfter) {
                            healthy = true
                        }
                        onFrame(event.text)
                    }
                    is SocketEvent.Closed -> {
                        Timber.tag(loggerTag.value).w("Socket closed: ${event.reason}")
                        break
                    }
                    is SocketEvent.Failure -> {
                        Timber.tag(loggerTag.value).w(event.throwable, "Socket failure")
                        break
                    }
                }
            }
        } finally {
            socket.cancel()
            events.close()
            if (openedAt != null) {
                onConnectedChanged(false)
            }
        }
        healthy
    }

    private suspend fun onFrame(text: String) {
        val frame = frameParser.parse(text)
        if (frame == null) {
            Timber.tag(loggerTag.value).w("Unparseable frame ignored")
            return
        }
        if (frame.isMessage) {
            messageHandler.handle(registration, frame)
        }
    }

    private fun buildUrl(lastMessageId: String?): String {
        val base = serverUrl.replaceFirst("https://", "wss://").replaceFirst("http://", "ws://")
        val since = lastMessageId?.takeIf { it.isNotBlank() }?.let { "?since=$it" }.orEmpty()
        return "$base/${registration.topic}/ws$since"
    }
}

/** Builds connections with the app-wide collaborators; the service supplies the per-connection callbacks. */
@Inject
class FeralPushConnectionFactory(
    private val socketFactory: FeralPushSocketFactory,
    private val frameParser: FeralPushFrameParser,
    private val messageHandler: FeralPushMessageHandler,
    private val feralPushStore: FeralPushStore,
) {
    fun create(
        registration: FeralPushRegistration,
        onConnectedChanged: (Boolean) -> Unit,
    ): FeralPushConnection {
        return FeralPushConnection(
            registration = registration,
            socketFactory = socketFactory,
            frameParser = frameParser,
            messageHandler = messageHandler,
            lastMessageIdProvider = { feralPushStore.get(registration.session)?.lastMessageId },
            onConnectedChanged = onConnectedChanged,
        )
    }
}

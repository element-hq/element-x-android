/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.feral.connection

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.appconfig.FeralPushConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

/** Minimal WebSocket surface, so [FeralPushConnection] can be unit-tested without OkHttp. */
interface FeralPushSocket {
    /** Close immediately and release resources; no further listener callback is needed. */
    fun cancel()
}

interface FeralPushSocketListener {
    fun onOpen()
    fun onFrame(text: String)
    fun onClosed(reason: String)
    fun onFailure(throwable: Throwable)
}

fun interface FeralPushSocketFactory {
    fun open(url: String, listener: FeralPushSocketListener): FeralPushSocket
}

@ContributesBinding(AppScope::class)
class DefaultFeralPushSocketFactory(
    okHttpClient: OkHttpClient,
) : FeralPushSocketFactory {
    private val client = okHttpClient.newBuilder()
        // A WebSocket is long-lived: no read timeout, but pings so a dead link is noticed.
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(FeralPushConfig.PING_INTERVAL.inWholeMilliseconds, TimeUnit.MILLISECONDS)
        .build()

    override fun open(url: String, listener: FeralPushSocketListener): FeralPushSocket {
        val webSocket = client.newWebSocket(
            Request.Builder().url(url).build(),
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) = listener.onOpen()

                override fun onMessage(webSocket: WebSocket, text: String) = listener.onFrame(text)

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    webSocket.close(code, null)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) = listener.onClosed("$code $reason")

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) = listener.onFailure(t)
            }
        )
        return object : FeralPushSocket {
            override fun cancel() = webSocket.cancel()
        }
    }
}

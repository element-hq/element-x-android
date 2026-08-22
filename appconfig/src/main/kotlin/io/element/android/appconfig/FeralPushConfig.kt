/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appconfig

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Feral built-in push provider.
 *
 * Members need no helper app: the app itself keeps a WebSocket open to the Feral ntfy server
 * (libraries/pushproviders/feral) from a foreground service, and the same server is the Matrix
 * push gateway, so the homeserver posts notifications to [GATEWAY_URL] with a `pushkey` of the
 * form `https://ntfy.feralisme.fr/<topic>?up=1`, which ntfy republishes on `<topic>`.
 */
object FeralPushConfig {
    /** The Feral ntfy server (UnifiedPush server + Matrix push gateway). */
    const val SERVER_URL: String = "https://ntfy.feralisme.fr"

    /** Matrix push gateway endpoint, the `url` of the registered pusher. */
    const val GATEWAY_URL: String = "$SERVER_URL/_matrix/push/v1/notify"

    /** Push provider name, stored per session in UserPushStore. */
    const val NAME: String = "Feral"

    /** Sorted before UnifiedPush (1) and Firebase (0 on gplay, absent on fdroid) so it is chosen first. */
    const val INDEX: Int = 0

    /** Pseudo distributor exposed by the provider (it has a single, built-in one). */
    const val DISTRIBUTOR_VALUE: String = "feral.builtin"
    const val DISTRIBUTOR_NAME: String = "Feral (built-in)"

    /** Topics starting with this prefix are anonymously readable and writable on the Feral server. */
    const val TOPIC_PREFIX: String = "up"

    /** Number of random bytes appended to [TOPIC_PREFIX] (hex-encoded: 32 chars). */
    const val TOPIC_RANDOM_BYTES: Int = 16

    /** Close and reconnect when no frame (keepalives every ~45 s count) was received for this long. */
    val WATCHDOG_TIMEOUT: Duration = 120.seconds

    /** Reconnect backoff bounds (exponential with jitter). */
    val BACKOFF_MIN: Duration = 1.seconds
    val BACKOFF_MAX: Duration = 60.seconds

    /** A connection that stayed open for this long is considered healthy: the backoff is reset. */
    val HEALTHY_AFTER: Duration = 60.seconds

    /** OkHttp WebSocket ping interval, so a dead link is noticed before the watchdog fires. */
    val PING_INTERVAL: Duration = 30.seconds
}

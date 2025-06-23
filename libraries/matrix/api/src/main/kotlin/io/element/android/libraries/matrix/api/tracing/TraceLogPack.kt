/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.tracing

enum class TraceLogPack(val key: String) {
    EVENT_CACHE("event_cache") {
        override val title: String = "Event Cache"
    },
    SEND_QUEUE("send_queue") {
        override val title: String = "Send Queue"
    },
    TIMELINE("timeline") {
        override val title: String = "Timeline"
    };

    abstract val title: String
}

/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.tracing

import io.element.android.libraries.matrix.api.tracing.TraceLogPack
import org.matrix.rustcomponents.sdk.TraceLogPacks as RustTraceLogPack

fun TraceLogPack.map(): RustTraceLogPack = when (this) {
    TraceLogPack.SEND_QUEUE -> RustTraceLogPack.SEND_QUEUE
    TraceLogPack.EVENT_CACHE -> RustTraceLogPack.EVENT_CACHE
    TraceLogPack.TIMELINE -> RustTraceLogPack.TIMELINE
}

fun Collection<TraceLogPack>.map(): List<RustTraceLogPack> {
    return map { it.map() }
}

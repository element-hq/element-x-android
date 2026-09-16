/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.matrix.api.notification.CallIntent
import io.element.android.libraries.matrix.api.room.CallIntentConsensus
import org.matrix.rustcomponents.sdk.RtcCallIntent
import org.matrix.rustcomponents.sdk.RtcCallIntentConsensus

fun RtcCallIntentConsensus.map(): CallIntentConsensus = when (this) {
    is RtcCallIntentConsensus.Full -> CallIntentConsensus.Full(v1.map())
    is RtcCallIntentConsensus.Partial -> CallIntentConsensus.Partial(
        callIntent = intent.map(),
        agreeingCount = agreeingCount.toInt(),
        totalCount = totalCount.toInt()
    )
    RtcCallIntentConsensus.None -> CallIntentConsensus.None
}

fun RtcCallIntent.map(): CallIntent = when (this) {
    RtcCallIntent.VIDEO -> CallIntent.VIDEO
    RtcCallIntent.AUDIO -> CallIntent.AUDIO
}

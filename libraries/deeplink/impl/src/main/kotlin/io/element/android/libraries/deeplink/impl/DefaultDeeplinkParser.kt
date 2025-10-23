/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.deeplink.impl

import android.content.Intent
import android.net.Uri
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.deeplink.api.DeeplinkData
import io.element.android.libraries.deeplink.api.DeeplinkParser
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId

@ContributesBinding(AppScope::class)
class DefaultDeeplinkParser : DeeplinkParser {
    override fun getFromIntent(intent: Intent): DeeplinkData? {
        return intent
            .takeIf { it.action == Intent.ACTION_VIEW }
            ?.data
            ?.toDeeplinkData()
    }

    private fun Uri.toDeeplinkData(): DeeplinkData? {
        if (scheme != SCHEME) return null
        if (host != HOST) return null
        val pathBits = path.orEmpty().split("/").drop(1)
        val sessionId = pathBits.elementAtOrNull(0)?.let(::SessionId) ?: return null

        return when (val screenPathComponent = pathBits.elementAtOrNull(1)) {
            null -> DeeplinkData.Root(sessionId)
            else -> {
                val roomId = screenPathComponent.let(::RoomId)
                val threadId = pathBits.elementAtOrNull(2)?.takeIf { it.isNotBlank() }?.let(::ThreadId)
                val eventId = pathBits.elementAtOrNull(3)?.takeIf { it.isNotBlank() }?.let(::EventId)
                DeeplinkData.Room(sessionId, roomId, threadId, eventId)
            }
        }
    }
}

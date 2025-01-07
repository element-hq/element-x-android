/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.deeplink

import android.content.Intent
import android.net.Uri
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import javax.inject.Inject

class DeeplinkParser @Inject constructor() {
    fun getFromIntent(intent: Intent): DeeplinkData? {
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
                val threadId = pathBits.elementAtOrNull(2)?.let(::ThreadId)
                DeeplinkData.Room(sessionId, roomId, threadId)
            }
        }
    }
}

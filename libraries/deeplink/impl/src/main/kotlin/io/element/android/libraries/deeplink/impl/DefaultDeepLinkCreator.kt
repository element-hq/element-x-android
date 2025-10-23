/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.deeplink.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.deeplink.api.DeepLinkCreator
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId

@ContributesBinding(AppScope::class)
class DefaultDeepLinkCreator : DeepLinkCreator {
    override fun create(sessionId: SessionId, roomId: RoomId?, threadId: ThreadId?, eventId: EventId?): String {
        return buildString {
            append("$SCHEME://$HOST/")
            append(sessionId.value)
            if (roomId != null) {
                append("/")
                append(roomId.value)
                if (threadId != null) {
                    append("/")
                    append(threadId.value)
                }

                if (eventId != null) {
                    if (threadId == null) {
                        // Add extra separator to mark the thread slot as empty
                        append("/")
                    }
                    append("/")
                    append(eventId.value)
                }
            }
        }
    }
}

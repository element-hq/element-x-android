/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.deeplink.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.deeplink.api.DeepLinkCreator
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultDeepLinkCreator @Inject constructor() : DeepLinkCreator {
    override fun room(sessionId: SessionId, roomId: RoomId?, threadId: ThreadId?): String {
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
            }
        }
    }
}

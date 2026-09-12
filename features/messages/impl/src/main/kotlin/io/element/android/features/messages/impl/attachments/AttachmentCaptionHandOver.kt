/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.timeline.Timeline

@SingleIn(RoomScope::class)
@Inject
class AttachmentCaptionHandOver {
    private var sentFor: Timeline.Mode? = null

    fun onSent(timelineMode: Timeline.Mode) {
        sentFor = timelineMode
    }

    fun consumeSent(timelineMode: Timeline.Mode): Boolean {
        if (sentFor != timelineMode) return false
        sentFor = null
        return true
    }
}

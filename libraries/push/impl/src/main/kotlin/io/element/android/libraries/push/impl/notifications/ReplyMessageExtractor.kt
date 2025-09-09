/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import android.content.Intent
import androidx.core.app.RemoteInput
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

interface ReplyMessageExtractor {
    fun getReplyMessage(intent: Intent): String?
}

@ContributesBinding(AppScope::class)
@Inject class AndroidReplyMessageExtractor : ReplyMessageExtractor {
    override fun getReplyMessage(intent: Intent): String? {
        return RemoteInput.getResultsFromIntent(intent)
            ?.getCharSequence(NotificationBroadcastReceiver.KEY_TEXT_REPLY)
            ?.toString()
    }
}

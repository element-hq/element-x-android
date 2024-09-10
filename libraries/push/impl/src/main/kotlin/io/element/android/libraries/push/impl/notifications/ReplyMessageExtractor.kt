/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import android.content.Intent
import androidx.core.app.RemoteInput
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

interface ReplyMessageExtractor {
    fun getReplyMessage(intent: Intent): String?
}

@ContributesBinding(AppScope::class)
class AndroidReplyMessageExtractor @Inject constructor() : ReplyMessageExtractor {
    override fun getReplyMessage(intent: Intent): String? {
        return RemoteInput.getResultsFromIntent(intent)
            ?.getCharSequence(NotificationBroadcastReceiver.KEY_TEXT_REPLY)
            ?.toString()
    }
}

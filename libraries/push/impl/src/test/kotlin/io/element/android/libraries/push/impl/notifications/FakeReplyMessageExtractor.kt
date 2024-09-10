/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import android.content.Intent

class FakeReplyMessageExtractor(
    private val result: String? = null,
) : ReplyMessageExtractor {
    override fun getReplyMessage(intent: Intent): String? {
        return result
    }
}

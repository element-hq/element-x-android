/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.appconfig

import android.graphics.Color
import androidx.annotation.ColorInt

object NotificationConfig {
    // TODO EAx Implement and set to true at some point
    const val SUPPORT_MARK_AS_READ_ACTION = false

    // TODO EAx Implement and set to true at some point
    const val SUPPORT_JOIN_DECLINE_INVITE = false

    // TODO EAx Implement and set to true at some point
    const val SUPPORT_QUICK_REPLY_ACTION = false

    @ColorInt
    val NOTIFICATION_ACCENT_COLOR: Int = Color.parseColor("#FF0DBD8B")
}

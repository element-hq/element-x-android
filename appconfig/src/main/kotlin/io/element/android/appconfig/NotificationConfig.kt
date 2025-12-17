/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appconfig

import androidx.annotation.ColorInt
import androidx.core.graphics.toColorInt

object NotificationConfig {
    /**
     * If set to true, the notification will have a "Mark as read" action.
     */
    const val SHOW_MARK_AS_READ_ACTION = true

    /**
     * If set to true, the notification for invitation will have two actions to accept or decline the invite.
     */
    const val SHOW_ACCEPT_AND_DECLINE_INVITE_ACTIONS = true

    /**
     * If set to true, the notification will have a "Quick reply" action, allow to compose and send a message to the room.
     */
    const val SHOW_QUICK_REPLY_ACTION = true

    @ColorInt
    val NOTIFICATION_ACCENT_COLOR: Int = "#FF0DBD8B".toColorInt()
}

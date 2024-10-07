/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

data class NotificationAction(
    val shouldNotify: Boolean,
    val highlight: Boolean,
    val soundName: String?
)

/*
fun List<Action>.toNotificationAction(): NotificationAction {
    var shouldNotify = false
    var highlight = false
    var sound: String? = null
    forEach { action ->
        when (action) {
            is Action.Notify -> shouldNotify = true
            is Action.DoNotNotify -> shouldNotify = false
            is Action.Highlight -> highlight = action.highlight
            is Action.Sound -> sound = action.sound
        }
    }
    return NotificationAction(shouldNotify, highlight, sound)
}
 */

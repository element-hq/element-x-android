/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.actionlist.model

fun interface TimelineItemActionPostProcessor {
    fun process(actions: List<TimelineItemAction>): List<TimelineItemAction>

    object Default : TimelineItemActionPostProcessor {
        override fun process(actions: List<TimelineItemAction>): List<TimelineItemAction> {
            return actions
        }
    }
}

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.utils.messagesummary

import io.element.android.features.messages.impl.timeline.model.TimelineItem

interface MessageSummaryFormatter {
    fun format(event: TimelineItem.Event): String
}

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.messagesummary

import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.utils.messagesummary.MessageSummaryFormatter

class FakeMessageSummaryFormatter : MessageSummaryFormatter {
    private var result = "A message"

    override fun format(content: TimelineItemEventContent): String = result

    fun givenMessageResult(value: String) {
        result = value
    }
}

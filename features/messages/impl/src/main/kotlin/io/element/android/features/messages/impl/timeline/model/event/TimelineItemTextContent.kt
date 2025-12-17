/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import io.element.android.libraries.matrix.ui.messages.toPlainText
import org.jsoup.nodes.Document

data class TimelineItemTextContent(
    override val body: String,
    override val htmlDocument: Document?,
    override val formattedBody: CharSequence,
    override val isEdited: Boolean,
) : TimelineItemTextBasedContent {
    override val type: String = "TimelineItemTextContent"
    override val plainText: String = htmlDocument?.toPlainText() ?: body
}

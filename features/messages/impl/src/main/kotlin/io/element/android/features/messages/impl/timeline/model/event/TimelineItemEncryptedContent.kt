/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import io.element.android.libraries.matrix.api.timeline.item.event.UnableToDecryptContent

data class TimelineItemEncryptedContent(
    val data: UnableToDecryptContent.Data
) : TimelineItemEventContent {
    override val type: String = "TimelineItemEncryptedContent"
}

/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import io.element.android.features.location.api.Location

data class TimelineItemLocationContent(
    val body: String,
    val location: Location,
    val description: String? = null,
) : TimelineItemEventContent {
    override val type: String = "TimelineItemLocationContent"
}

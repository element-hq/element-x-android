/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.virtual

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class TimelineItemDaySeparatorModelProvider : PreviewParameterProvider<TimelineItemDaySeparatorModel> {
    override val values = sequenceOf(
        aTimelineItemDaySeparatorModel("Today"),
        aTimelineItemDaySeparatorModel("March 6, 2023")
    )
}

fun aTimelineItemDaySeparatorModel(formattedDate: String) = TimelineItemDaySeparatorModel(
    formattedDate = formattedDate
)

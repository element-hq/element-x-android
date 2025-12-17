/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl.previews

data class DateForPreview(
    val semantic: String,
    val date: String,
)

val dateForPreviewToday = DateForPreview(
    semantic = "Today",
    date = "1980-04-06T18:35:24.00Z",
)

val dateForPreviews = listOf(
    DateForPreview(
        semantic = "Now",
        date = dateForPreviewToday.date,
    ),
    DateForPreview(
        semantic = "One second ago",
        date = "1980-04-06T18:35:23.00Z",
    ),
    DateForPreview(
        semantic = "One minute ago",
        date = "1980-04-06T18:34:24.00Z",
    ),
    DateForPreview(
        semantic = "One hour ago",
        date = "1980-04-06T17:35:24.00Z",
    ),
    DateForPreview(
        semantic = "One day ago",
        date = "1980-04-05T18:35:24.00Z",
    ),
    DateForPreview(
        semantic = "Two days ago",
        date = "1980-04-04T18:35:24.00Z",
    ),
    DateForPreview(
        semantic = "One month ago",
        date = "1980-03-06T18:35:24.00Z",
    ),
    DateForPreview(
        semantic = "One year ago",
        date = "1979-04-06T18:35:24.00Z",
    ),
)

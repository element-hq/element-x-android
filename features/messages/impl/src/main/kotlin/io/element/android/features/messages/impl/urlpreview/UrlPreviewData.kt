/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.urlpreview

data class UrlPreviewData(
    val url: String,
    val title: String?,
    val description: String?,
    val imageUrl: String?,
    val siteName: String?,
    val hostName: String,
)

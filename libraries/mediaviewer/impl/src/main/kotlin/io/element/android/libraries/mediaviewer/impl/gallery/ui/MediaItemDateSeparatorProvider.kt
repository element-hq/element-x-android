/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import io.element.android.libraries.mediaviewer.impl.model.aMediaItemDateSeparator

class MediaItemDateSeparatorProvider : PreviewParameterProvider<MediaItem.DateSeparator> {
    override val values: Sequence<MediaItem.DateSeparator>
        get() = sequenceOf(
            aMediaItemDateSeparator(),
            aMediaItemDateSeparator(formattedDate = "A long date that should be truncated"),
        )
}

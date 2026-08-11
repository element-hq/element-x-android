/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.ui.media.contentvalidation.ContentValidationValue
import io.element.android.libraries.mediaviewer.impl.model.MediaItem

@Composable
fun FileItemView(
    file: MediaItem.File,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    contentValidationState: ContentValidationValue,
    modifier: Modifier = Modifier,
) {
    GalleryFileListItem(
        modifier = modifier,
        contentValidationValue = contentValidationState,
        caption = file.mediaInfo.caption
    ) {
        GalleryFileListItemContent(
            name = file.mediaInfo.filename,
            formattedSize = file.mediaInfo.formattedFileSize,
            icon = CompoundIcons.Attachment(),
            onClick = onClick,
            isValidating = contentValidationState.isLoading(),
            onLongClick = onLongClick,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun FileItemViewPreview(
    @PreviewParameter(MediaItemFileProvider::class) file: MediaItem.File,
) = ElementPreview {
    val states = remember {
        listOf(
            ContentValidationValue.Valid,
            ContentValidationValue.Loading,
            ContentValidationValue.Invalid,
            ContentValidationValue.UnrecoverableError(Throwable("Unrecoverable error")),
        )
    }
    Column {
        for (state in states) {
            FileItemView(
                file = file,
                onClick = {},
                onLongClick = {},
                contentValidationState = state,
            )
        }
    }
}

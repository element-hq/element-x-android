/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.viewfolder.impl.folder

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.viewfolder.impl.model.Item
import kotlinx.collections.immutable.toImmutableList

open class ViewFolderStateProvider : PreviewParameterProvider<ViewFolderState> {
    override val values: Sequence<ViewFolderState>
        get() = sequenceOf(
            aViewFolderState(),
            aViewFolderState(
                content = listOf(
                    Item.Parent,
                    Item.Folder("aPath", "aFolder"),
                    Item.File("aPath", "aFile", "12kB"),
                )
            )
        )
}

fun aViewFolderState(
    title: String = "aPath",
    content: List<Item> = emptyList(),
) = ViewFolderState(
    title = title,
    content = content.toImmutableList(),
)

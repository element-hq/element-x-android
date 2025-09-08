/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.viewfolder.impl.folder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.Inject
import io.element.android.features.viewfolder.impl.model.Item
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta
import kotlinx.collections.immutable.toImmutableList

@Inject
class ViewFolderPresenter(
    @Assisted val canGoUp: Boolean,
    @Assisted val path: String,
    private val folderExplorer: FolderExplorer,
    private val buildMeta: BuildMeta,
) : Presenter<ViewFolderState> {
    @AssistedFactory
    interface Factory {
        fun create(canGoUp: Boolean, path: String): ViewFolderPresenter
    }

    @Composable
    override fun present(): ViewFolderState {
        var content by remember { mutableStateOf(emptyList<Item>()) }
        val title = remember {
            buildString {
                if (path.contains(buildMeta.applicationId)) {
                    append("…")
                }
                append(path.substringAfter(buildMeta.applicationId))
            }
        }
        LaunchedEffect(Unit) {
            content = buildList {
                if (canGoUp) add(Item.Parent)
                addAll(folderExplorer.getItems(path))
            }
        }
        return ViewFolderState(
            title = title,
            content = content.toImmutableList(),
        )
    }
}

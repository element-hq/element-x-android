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
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.viewfolder.impl.model.Item
import io.element.android.libraries.architecture.Presenter
import kotlinx.collections.immutable.toImmutableList

class ViewFolderPresenter @AssistedInject constructor(
    @Assisted val canGoUp: Boolean,
    @Assisted val path: String,
    private val folderExplorer: FolderExplorer,
) : Presenter<ViewFolderState> {
    @AssistedFactory
    interface Factory {
        fun create(canGoUp: Boolean, path: String): ViewFolderPresenter
    }

    @Composable
    override fun present(): ViewFolderState {
        var content by remember { mutableStateOf(emptyList<Item>()) }
        LaunchedEffect(Unit) {
            content = buildList {
                if (canGoUp) add(Item.Parent)
                addAll(folderExplorer.getItems(path))
            }
        }
        return ViewFolderState(
            path = path,
            content = content.toImmutableList(),
        )
    }
}

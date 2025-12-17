/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.viewfolder.impl.folder

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.viewfolder.impl.model.Item
import io.element.android.libraries.androidutils.filesize.FileSizeFormatter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import kotlinx.coroutines.withContext
import java.io.File

interface FolderExplorer {
    suspend fun getItems(path: String): List<Item>
}

@ContributesBinding(AppScope::class)
class DefaultFolderExplorer(
    private val fileSizeFormatter: FileSizeFormatter,
    private val dispatchers: CoroutineDispatchers,
) : FolderExplorer {
    override suspend fun getItems(path: String): List<Item> = withContext(dispatchers.io) {
        val current = File(path)
        if (current.isFile) {
            error("Not a folder")
        }
        val folderContent = current.listFiles().orEmpty().map { file ->
            if (file.isDirectory) {
                Item.Folder(
                    path = file.path,
                    name = file.name
                )
            } else {
                Item.File(
                    path = file.path,
                    name = file.name,
                    formattedSize = fileSizeFormatter.format(file.length()),
                )
            }
        }
        buildList {
            addAll(folderContent.filterIsInstance<Item.Folder>().sortedBy(Item.Folder::name))
            addAll(folderContent.filterIsInstance<Item.File>().sortedBy(Item.File::name))
        }
    }
}

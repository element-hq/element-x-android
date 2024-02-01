/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.viewfolder.impl.folder

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.viewfolder.impl.model.Item
import io.element.android.libraries.androidutils.filesize.FileSizeFormatter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.AppScope
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

interface FolderExplorer {
    suspend fun getItems(path: String): List<Item>
}

@ContributesBinding(AppScope::class)
class DefaultFolderExplorer @Inject constructor(
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

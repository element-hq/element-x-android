/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.viewfolder.impl.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import dev.zacsweers.metro.Inject
import io.element.android.features.viewfolder.impl.file.ViewFileNode
import io.element.android.features.viewfolder.impl.folder.ViewFolderNode
import io.element.android.features.viewfolder.impl.model.Item
import io.element.android.libraries.architecture.ElementNavDisplay
import kotlinx.serialization.Serializable

@Inject
class ViewFolderFlowNode(
    private val viewFolderNode: ViewFolderNode,
    private val viewFileNode: ViewFileNode,
) {
    sealed interface NavEntry : NavKey {
        @Serializable
        data class Root(val rootPath: String) : NavEntry

        @Serializable
        data class Folder(val path: String) : NavEntry

        @Serializable
        data class File(val path: String, val name: String) : NavEntry
    }

    @Composable
    fun View(
        rootPath: String,
        onDone: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val backStack = rememberNavBackStack(NavEntry.Root(rootPath))
        ElementNavDisplay(
            backStack = backStack,
            onBack = { if (backStack.size <= 1) onDone() else backStack.removeLastOrNull() },
            modifier = modifier,
            entryProvider = entryProvider {
                entry<NavEntry.Root> { key ->
                    viewFolderNode.View(
                        canGoUp = false,
                        path = key.rootPath,
                        onBackClick = onDone,
                        onNavigateToItem = { item ->
                            when (item) {
                                Item.Parent -> Unit
                                is Item.Folder -> backStack.add(NavEntry.Folder(item.path))
                                is Item.File -> backStack.add(NavEntry.File(item.path, item.name))
                            }
                        },
                    )
                }
                entry<NavEntry.Folder> { key ->
                    viewFolderNode.View(
                        canGoUp = true,
                        path = key.path,
                        onBackClick = backStack::removeLastOrNull,
                        onNavigateToItem = { item ->
                            when (item) {
                                Item.Parent -> backStack.removeLastOrNull()
                                is Item.Folder -> backStack.add(NavEntry.Folder(item.path))
                                is Item.File -> backStack.add(NavEntry.File(item.path, item.name))
                            }
                        },
                    )
                }
                entry<NavEntry.File> { key ->
                    viewFileNode.View(
                        path = key.path,
                        name = key.name,
                        onBackClick = backStack::removeLastOrNull,
                    )
                }
            },
        )
    }
}

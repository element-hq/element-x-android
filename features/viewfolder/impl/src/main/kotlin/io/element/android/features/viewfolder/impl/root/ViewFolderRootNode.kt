/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.viewfolder.impl.root

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.viewfolder.api.ViewFolderEntryPoint
import io.element.android.features.viewfolder.impl.file.ViewFileNode
import io.element.android.features.viewfolder.impl.folder.ViewFolderNode
import io.element.android.features.viewfolder.impl.model.Item
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.AppScope
import kotlinx.parcelize.Parcelize

@ContributesNode(AppScope::class)
class ViewFolderRootNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
) : BaseFlowNode<ViewFolderRootNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data class Folder(
            val path: String,
        ) : NavTarget

        @Parcelize
        data class File(
            val path: String,
            val name: String,
        ) : NavTarget
    }

    data class Inputs(
        val rootPath: String,
    ) : NodeInputs

    private val inputs: Inputs = inputs()

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.Root -> {
                createViewFolderNode(
                    buildContext,
                    inputs = ViewFolderNode.Inputs(
                        canGoUp = false,
                        path = inputs.rootPath,
                    )
                )
            }
            is NavTarget.Folder -> {
                createViewFolderNode(
                    buildContext,
                    inputs = ViewFolderNode.Inputs(
                        canGoUp = true,
                        path = navTarget.path,
                    )
                )
            }
            is NavTarget.File -> {
                val callback: ViewFileNode.Callback = object : ViewFileNode.Callback {
                    override fun onBackClick() {
                        backstack.pop()
                    }
                }
                val inputs = ViewFileNode.Inputs(
                    path = navTarget.path,
                    name = navTarget.name,
                )
                createNode<ViewFileNode>(buildContext, plugins = listOf(inputs, callback))
            }
        }
    }

    private fun createViewFolderNode(
        buildContext: BuildContext,
        inputs: ViewFolderNode.Inputs,
    ): Node {
        val callback: ViewFolderNode.Callback = object : ViewFolderNode.Callback {
            override fun onBackClick() {
                onDone()
            }

            override fun onNavigateTo(item: Item) {
                when (item) {
                    Item.Parent -> {
                        // Should not happen when in Root since parent is not accessible from root (canGoUp set to false)
                        backstack.pop()
                    }
                    is Item.Folder -> {
                        backstack.push(NavTarget.Folder(path = item.path))
                    }
                    is Item.File -> {
                        backstack.push(NavTarget.File(path = item.path, name = item.name))
                    }
                }
            }
        }
        return createNode<ViewFolderNode>(buildContext, plugins = listOf(inputs, callback))
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }

    private fun onDone() {
        plugins<ViewFolderEntryPoint.Callback>().forEach { it.onDone() }
    }
}

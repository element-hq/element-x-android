/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.viewfolder.impl.folder

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.Inject
import io.element.android.annotations.ContributesNode
import io.element.android.features.viewfolder.impl.model.Item
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs

@ContributesNode(AppScope::class)
@Inject
class ViewFolderNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: ViewFolderPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(
        val canGoUp: Boolean,
        val path: String,
    ) : NodeInputs

    interface Callback : Plugin {
        fun onBackClick()
        fun onNavigateTo(item: Item)
    }

    private val inputs: Inputs = inputs()

    private val presenter = presenterFactory.create(
        canGoUp = inputs.canGoUp,
        path = inputs.path,
    )

    private fun onBackClick() {
        plugins<Callback>().forEach { it.onBackClick() }
    }

    private fun onNavigateTo(item: Item) {
        plugins<Callback>().forEach { it.onNavigateTo(item) }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        ViewFolderView(
            state = state,
            modifier = modifier,
            onNavigateTo = ::onNavigateTo,
            onBackClick = ::onBackClick,
        )
    }
}

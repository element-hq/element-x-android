/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.viewfolder.impl.file

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.Inject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import dev.zacsweers.metro.AppScope

@ContributesNode(AppScope::class)
@Inject
class ViewFileNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: ViewFilePresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(
        val path: String,
        val name: String,
    ) : NodeInputs

    interface Callback : Plugin {
        fun onBackClick()
    }

    private val inputs: Inputs = inputs()

    private val presenter = presenterFactory.create(
        path = inputs.path,
        name = inputs.name,
    )

    private fun onBackClick() {
        plugins<Callback>().forEach { it.onBackClick() }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        ViewFileView(
            state = state,
            modifier = modifier,
            onBackClick = ::onBackClick,
        )
    }
}

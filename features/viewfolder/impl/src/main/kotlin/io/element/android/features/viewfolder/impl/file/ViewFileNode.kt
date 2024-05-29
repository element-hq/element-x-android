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

package io.element.android.features.viewfolder.impl.file

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.AppScope

@ContributesNode(AppScope::class)
class ViewFileNode @AssistedInject constructor(
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

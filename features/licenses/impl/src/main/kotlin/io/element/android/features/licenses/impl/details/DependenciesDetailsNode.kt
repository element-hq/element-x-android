/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.licenses.impl.details

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.licenses.impl.model.DependencyLicenseItem
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.AppScope

@ContributesNode(AppScope::class)
class DependenciesDetailsNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
) : Node(
    buildContext = buildContext,
    plugins = plugins
) {
    data class Inputs(
        val licenseItem: DependencyLicenseItem,
    ) : NodeInputs

    private val licenseItem = inputs<Inputs>().licenseItem

    @Composable
    override fun View(modifier: Modifier) {
        DependenciesDetailsView(
            modifier = modifier,
            licenseItem = licenseItem,
            onBack = ::navigateUp
        )
    }
}

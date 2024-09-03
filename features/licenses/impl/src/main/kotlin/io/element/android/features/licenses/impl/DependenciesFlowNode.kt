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

package io.element.android.features.licenses.impl

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.licenses.impl.details.DependenciesDetailsNode
import io.element.android.features.licenses.impl.list.DependencyLicensesListNode
import io.element.android.features.licenses.impl.model.DependencyLicenseItem
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import kotlinx.parcelize.Parcelize

@ContributesNode(AppScope::class)
class DependenciesFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
) : BaseFlowNode<DependenciesFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.LicensesList,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object LicensesList : NavTarget

        @Parcelize
        data class LicenseDetails(val license: DependencyLicenseItem) : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.LicensesList -> {
                val callback = object : DependencyLicensesListNode.Callback {
                    override fun onOpenLicense(license: DependencyLicenseItem) {
                        backstack.push(NavTarget.LicenseDetails(license))
                    }
                }
                createNode<DependencyLicensesListNode>(buildContext, listOf(callback))
            }
            is NavTarget.LicenseDetails -> {
                createNode<DependenciesDetailsNode>(buildContext, listOf(DependenciesDetailsNode.Inputs(navTarget.license)))
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView(modifier)
    }
}

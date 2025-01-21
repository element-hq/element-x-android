/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
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
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.licenses.impl.details.DependenciesDetailsNode
import io.element.android.features.licenses.impl.list.DependencyLicensesListNode
import io.element.android.features.licenses.impl.model.DependencyLicenseItem
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.createNode
import kotlinx.parcelize.Parcelize

@ContributesNode(AppScope::class)
@AssistedInject
class DependenciesFlowNode(
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
                    override fun navigateToLicense(license: DependencyLicenseItem) {
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

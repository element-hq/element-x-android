/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.licenses.impl.details

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.licenses.impl.model.DependencyLicenseItem
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs

@ContributesNode(AppScope::class)
@AssistedInject
class DependenciesDetailsNode(
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

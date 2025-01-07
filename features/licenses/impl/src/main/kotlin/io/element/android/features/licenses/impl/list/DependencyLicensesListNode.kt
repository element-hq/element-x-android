/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.licenses.impl.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.licenses.impl.model.DependencyLicenseItem
import io.element.android.libraries.di.AppScope

@ContributesNode(AppScope::class)
class DependencyLicensesListNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: DependencyLicensesListPresenter,
) : Node(
    buildContext = buildContext,
    plugins = plugins
) {
    interface Callback : Plugin {
        fun onOpenLicense(license: DependencyLicenseItem)
    }

    private fun onOpenLicense(license: DependencyLicenseItem) {
        plugins<Callback>()
            .forEach { it.onOpenLicense(license) }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        DependencyLicensesListView(
            state = state,
            onBackClick = ::navigateUp,
            onOpenLicense = ::onOpenLicense,
        )
    }
}

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.changeaccountprovider

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.login.impl.util.openLearnMorePage
import io.element.android.libraries.di.AppScope

@ContributesNode(AppScope::class)
class ChangeAccountProviderNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: ChangeAccountProviderPresenter,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun onDone()
        fun onOtherClick()
    }

    private fun onDone() {
        plugins<Callback>().forEach { it.onDone() }
    }

    private fun onOtherClick() {
        plugins<Callback>().forEach { it.onOtherClick() }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val context = LocalContext.current
        ChangeAccountProviderView(
            state = state,
            modifier = modifier,
            onBackClick = ::navigateUp,
            onLearnMoreClick = { openLearnMorePage(context) },
            onSuccess = ::onDone,
            onOtherProviderClick = ::onOtherClick,
        )
    }
}

/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.chooseaccountprovider

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
import io.element.android.libraries.matrix.api.auth.OidcDetails

@ContributesNode(AppScope::class)
class ChooseAccountProviderNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: ChooseAccountProviderPresenter,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun onLoginPasswordNeeded()
        fun onOidcDetails(oidcDetails: OidcDetails)
        fun onCreateAccountContinue(url: String)
    }

    private fun onOidcDetails(oidcDetails: OidcDetails) {
        plugins<Callback>().forEach { it.onOidcDetails(oidcDetails) }
    }

    private fun onLoginPasswordNeeded() {
        plugins<Callback>().forEach { it.onLoginPasswordNeeded() }
    }

    private fun onCreateAccountContinue(url: String) {
        plugins<Callback>().forEach { it.onCreateAccountContinue(url) }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val context = LocalContext.current
        ChooseAccountProviderView(
            state = state,
            modifier = modifier,
            onBackClick = ::navigateUp,
            onOidcDetails = ::onOidcDetails,
            onNeedLoginPassword = ::onLoginPasswordNeeded,
            onLearnMoreClick = { openLearnMorePage(context) },
            onCreateAccountContinue = ::onCreateAccountContinue,
        )
    }
}

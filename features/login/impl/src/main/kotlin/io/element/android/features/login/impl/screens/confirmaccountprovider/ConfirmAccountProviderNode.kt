/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.confirmaccountprovider

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.login.impl.util.openLearnMorePage
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.matrix.api.auth.OidcDetails

@ContributesNode(AppScope::class)
@AssistedInject
class ConfirmAccountProviderNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: ConfirmAccountProviderPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(
        val isAccountCreation: Boolean,
    ) : NodeInputs

    private val inputs: Inputs = inputs()
    private val presenter = presenterFactory.create(
        ConfirmAccountProviderPresenter.Params(
            isAccountCreation = inputs.isAccountCreation,
        )
    )

    interface Callback : Plugin {
        fun navigateToLoginPassword()
        fun navigateToOidc(oidcDetails: OidcDetails)
        fun navigateToCreateAccount(url: String)
        fun navigateToChangeAccountProvider()
    }

    private val callback: Callback = callback()

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val context = LocalContext.current
        ConfirmAccountProviderView(
            state = state,
            modifier = modifier,
            onOidcDetails = callback::navigateToOidc,
            onNeedLoginPassword = callback::navigateToLoginPassword,
            onCreateAccountContinue = callback::navigateToCreateAccount,
            onChange = callback::navigateToChangeAccountProvider,
            onLearnMoreClick = { openLearnMorePage(context) },
        )
    }
}

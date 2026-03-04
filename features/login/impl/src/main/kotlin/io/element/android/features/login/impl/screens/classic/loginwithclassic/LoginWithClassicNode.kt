/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.classic.loginwithclassic

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
import io.element.android.libraries.matrix.api.core.UserId

@ContributesNode(AppScope::class)
@AssistedInject
class LoginWithClassicNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: LoginWithClassicPresenter.Factory,
) : Node(buildContext, plugins = plugins),
    LoginWithClassicNavigator {
    interface Callback : Plugin {
        fun navigateToOtherOptions()
        fun navigateToLoginPassword()
        fun navigateToOidc(oidcDetails: OidcDetails)
        fun navigateToCreateAccount(url: String)
        fun navigateToMissingKeyBackup()
    }

    data class Inputs(
        val userId: UserId,
    ) : NodeInputs

    private val inputs: Inputs = inputs()
    val presenter = presenterFactory.create(inputs.userId, this)
    private val callback: Callback = callback()

    override fun navigateToMissingKeyBackup() {
        callback.navigateToMissingKeyBackup()
    }

    @Composable
    override fun View(modifier: Modifier) {
        val context = LocalContext.current
        val state = presenter.present()
        LoginWithClassicView(
            state = state,
            modifier = modifier,
            onOtherOptionsClick = callback::navigateToOtherOptions,
            onOidcDetails = callback::navigateToOidc,
            onNeedLoginPassword = callback::navigateToLoginPassword,
            onLearnMoreClick = { openLearnMorePage(context) },
            onCreateAccountContinue = callback::navigateToCreateAccount,
        )
    }
}

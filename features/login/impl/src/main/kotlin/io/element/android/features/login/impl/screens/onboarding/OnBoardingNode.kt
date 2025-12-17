/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.onboarding

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
class OnBoardingNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: OnBoardingPresenter.Factory,
) : Node(
    buildContext = buildContext,
    plugins = plugins
) {
    interface Callback : Plugin {
        fun navigateToSignUpFlow()
        fun navigateToSignInFlow(mustChooseAccountProvider: Boolean)
        fun navigateToQrCode()
        fun navigateToBugReport()
        fun navigateToLoginPassword()
        fun navigateToOidc(oidcDetails: OidcDetails)
        fun navigateToCreateAccount(url: String)
        fun onDone()
    }

    data class Params(
        val accountProvider: String?,
        val loginHint: String?,
    ) : NodeInputs

    private val callback: Callback = callback()
    private val params = inputs<Params>()

    private val presenter = presenterFactory.create(
        params = params,
    )

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val context = LocalContext.current
        OnBoardingView(
            state = state,
            modifier = modifier,
            onSignIn = callback::navigateToSignInFlow,
            onCreateAccount = callback::navigateToSignUpFlow,
            onSignInWithQrCode = callback::navigateToQrCode,
            onReportProblem = callback::navigateToBugReport,
            onOidcDetails = callback::navigateToOidc,
            onNeedLoginPassword = callback::navigateToLoginPassword,
            onLearnMoreClick = { openLearnMorePage(context) },
            onCreateAccountContinue = callback::navigateToCreateAccount,
            onBackClick = callback::onDone,
        )
    }
}

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.onboarding

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
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.auth.OidcDetails

@ContributesNode(AppScope::class)
class OnBoardingNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: OnBoardingPresenter.Factory,
) : Node(
    buildContext = buildContext,
    plugins = plugins
) {
    interface Callback : Plugin {
        fun onSignUp()
        fun onSignIn(mustChooseAccountProvider: Boolean)
        fun onSignInWithQrCode()
        fun onReportProblem()
        fun onLoginPasswordNeeded()
        fun onOidcDetails(oidcDetails: OidcDetails)
        fun onCreateAccountContinue(url: String)
    }

    data class Params(
        val accountProvider: String?,
        val loginHint: String?,
    ) : NodeInputs

    private val params = inputs<Params>()

    private val presenter = presenterFactory.create(
        params = params,
    )

    private fun onSignIn(mustChooseAccountProvider: Boolean) {
        plugins<Callback>().forEach { it.onSignIn(mustChooseAccountProvider) }
    }

    private fun onSignUp() {
        plugins<Callback>().forEach { it.onSignUp() }
    }

    private fun onSignInWithQrCode() {
        plugins<Callback>().forEach { it.onSignInWithQrCode() }
    }

    private fun onReportProblem() {
        plugins<Callback>().forEach { it.onReportProblem() }
    }

    private fun onOidcDetails(data: OidcDetails) {
        plugins<Callback>().forEach { it.onOidcDetails(data) }
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
        OnBoardingView(
            state = state,
            modifier = modifier,
            onSignIn = ::onSignIn,
            onCreateAccount = ::onSignUp,
            onSignInWithQrCode = ::onSignInWithQrCode,
            onReportProblem = ::onReportProblem,
            onOidcDetails = ::onOidcDetails,
            onNeedLoginPassword = ::onLoginPasswordNeeded,
            onLearnMoreClick = { openLearnMorePage(context) },
            onCreateAccountContinue = ::onCreateAccountContinue,
        )
    }
}

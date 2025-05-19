/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.di.AppScope

@ContributesNode(AppScope::class)
class OnBoardingNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: OnBoardingPresenter,
) : Node(
    buildContext = buildContext,
    plugins = plugins
) {
    interface Callback : Plugin {
        fun onSignUp()
        fun onSignIn()
        fun onSignInWithQrCode()
        fun onReportProblem()
    }

    private fun onSignIn() {
        plugins<Callback>().forEach { it.onSignIn() }
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

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        OnBoardingView(
            state = state,
            modifier = modifier,
            onSignIn = ::onSignIn,
            onCreateAccount = ::onSignUp,
            onSignInWithQrCode = ::onSignInWithQrCode,
            onReportProblem = ::onReportProblem,
        )
    }
}

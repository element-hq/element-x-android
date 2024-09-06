/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.onboarding.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.onboarding.api.OnBoardingEntryPoint
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
    private fun onSignIn() {
        plugins<OnBoardingEntryPoint.Callback>().forEach { it.onSignIn() }
    }

    private fun onSignUp() {
        plugins<OnBoardingEntryPoint.Callback>().forEach { it.onSignUp() }
    }

    private fun onSignInWithQrCode() {
        plugins<OnBoardingEntryPoint.Callback>().forEach { it.onSignInWithQrCode() }
    }

    private fun onOpenDeveloperSettings() {
        plugins<OnBoardingEntryPoint.Callback>().forEach { it.onOpenDeveloperSettings() }
    }

    private fun onReportProblem() {
        plugins<OnBoardingEntryPoint.Callback>().forEach { it.onReportProblem() }
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
            onOpenDeveloperSettings = ::onOpenDeveloperSettings,
            onReportProblem = ::onReportProblem,
        )
    }
}

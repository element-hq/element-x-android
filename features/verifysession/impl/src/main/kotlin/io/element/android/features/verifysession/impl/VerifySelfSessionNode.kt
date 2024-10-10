/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.verifysession.impl

import android.app.Activity
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
import io.element.android.appconfig.LearnMoreConfig
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.logout.api.util.onSuccessLogout
import io.element.android.features.verifysession.api.VerifySessionEntryPoint
import io.element.android.libraries.androidutils.browser.openUrlInChromeCustomTab
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
class VerifySelfSessionNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: VerifySelfSessionPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    private val callback = plugins<VerifySessionEntryPoint.Callback>().first()

    private val presenter = presenterFactory.create(
        showDeviceVerifiedScreen = inputs<VerifySessionEntryPoint.Params>().showDeviceVerifiedScreen,
    )

    private fun onLearnMoreClick(activity: Activity, dark: Boolean) {
        activity.openUrlInChromeCustomTab(null, dark, LearnMoreConfig.ENCRYPTION_URL)
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val activity = LocalContext.current as Activity
        val isDark = ElementTheme.isLightTheme.not()
        VerifySelfSessionView(
            state = state,
            modifier = modifier,
            onLearnMoreClick = {
                onLearnMoreClick(activity, isDark)
            },
            onEnterRecoveryKey = callback::onEnterRecoveryKey,
            onResetKey = callback::onResetKey,
            onFinish = callback::onDone,
            onSuccessLogout = { onSuccessLogout(activity, isDark, it) },
        )
    }
}

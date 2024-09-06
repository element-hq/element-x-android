/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.logout.impl

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
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.logout.api.LogoutEntryPoint
import io.element.android.features.logout.api.util.onSuccessLogout
import io.element.android.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
class LogoutNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: LogoutPresenter,
) : Node(buildContext, plugins = plugins) {
    private fun onChangeRecoveryKeyClick() {
        plugins<LogoutEntryPoint.Callback>().forEach { it.onChangeRecoveryKeyClick() }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val activity = LocalContext.current as Activity
        val isDark = ElementTheme.isLightTheme.not()
        LogoutView(
            state = state,
            onChangeRecoveryKeyClick = ::onChangeRecoveryKeyClick,
            onSuccessLogout = { onSuccessLogout(activity, isDark, it) },
            onBackClick = ::navigateUp,
            modifier = modifier,
        )
    }
}

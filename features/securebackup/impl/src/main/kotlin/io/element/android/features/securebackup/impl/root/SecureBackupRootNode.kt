/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.appconfig.LearnMoreConfig
import io.element.android.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
class SecureBackupRootNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: SecureBackupRootPresenter,
) : Node(
    buildContext = buildContext,
    plugins = plugins
) {
    interface Callback : Plugin {
        fun onSetupClick()
        fun onChangeClick()
        fun onDisableClick()
        fun onEnableClick()
        fun onConfirmRecoveryKeyClick()
    }

    private fun onSetupClick() {
        plugins<Callback>().forEach { it.onSetupClick() }
    }

    private fun onChangeClick() {
        plugins<Callback>().forEach { it.onChangeClick() }
    }

    private fun onDisableClick() {
        plugins<Callback>().forEach { it.onDisableClick() }
    }

    private fun onEnableClick() {
        plugins<Callback>().forEach { it.onEnableClick() }
    }

    private fun onConfirmRecoveryKeyClick() {
        plugins<Callback>().forEach { it.onConfirmRecoveryKeyClick() }
    }

    private fun onLearnMoreClick(uriHandler: UriHandler) {
        uriHandler.openUri(LearnMoreConfig.SECURE_BACKUP_URL)
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val uriHandler = LocalUriHandler.current
        SecureBackupRootView(
            state = state,
            onBackClick = ::navigateUp,
            onSetupClick = ::onSetupClick,
            onChangeClick = ::onChangeClick,
            onEnableClick = ::onEnableClick,
            onDisableClick = ::onDisableClick,
            onConfirmRecoveryKeyClick = ::onConfirmRecoveryKeyClick,
            onLearnMoreClick = { onLearnMoreClick(uriHandler) },
            modifier = modifier,
        )
    }
}

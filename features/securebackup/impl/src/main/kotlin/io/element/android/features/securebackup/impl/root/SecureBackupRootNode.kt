/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.appconfig.LearnMoreConfig
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
@AssistedInject
class SecureBackupRootNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: SecureBackupRootPresenter,
) : Node(
    buildContext = buildContext,
    plugins = plugins
) {
    interface Callback : Plugin {
        fun navigateToSetup()
        fun navigateToChange()
        fun navigateToDisable()
        fun navigateToEnterRecoveryKey()
    }

    private val callback: Callback = callback()

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
            onSetupClick = callback::navigateToSetup,
            onChangeClick = callback::navigateToChange,
            onDisableClick = callback::navigateToDisable,
            onConfirmRecoveryKeyClick = callback::navigateToEnterRecoveryKey,
            onLearnMoreClick = { onLearnMoreClick(uriHandler) },
            modifier = modifier,
        )
    }
}

/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.logout.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.appconfig.LearnMoreConfig
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
@AssistedInject
class LogoutNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: LogoutPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(
        /** Whether the user has just finished with their recovery key, which means that the device is removed once the keys are backed up. */
        val removeOnceKeysAreBackedUp: Boolean,
    ) : NodeInputs

    interface Callback : Plugin {
        fun onDismiss()
        fun onGenerateRecoveryKey(isReplacingExistingKey: Boolean)
        fun onCheckRecoveryKey()
        fun onOpenSecureBackupSettings()
    }

    private val inputs = inputs<Inputs>()
    private val callback: Callback = callback()
    private val presenter = presenterFactory.create(inputs.removeOnceKeysAreBackedUp)

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val uriHandler = LocalUriHandler.current
        LogoutView(
            state = state,
            onDismiss = callback::onDismiss,
            onGenerateRecoveryKeyClick = callback::onGenerateRecoveryKey,
            onCheckRecoveryKeyClick = callback::onCheckRecoveryKey,
            onOpenSettingsClick = callback::onOpenSecureBackupSettings,
            onLearnMoreClick = { uriHandler.openUri(LearnMoreConfig.SECURE_BACKUP_URL) },
            modifier = modifier,
        )
    }
}

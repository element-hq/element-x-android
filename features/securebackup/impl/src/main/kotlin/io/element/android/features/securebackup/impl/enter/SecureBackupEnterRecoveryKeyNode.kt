/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.enter

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
class SecureBackupEnterRecoveryKeyNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: SecureBackupEnterRecoveryKeyPresenter,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun onEnterRecoveryKeySuccess()
    }

    private val callback = plugins<Callback>().first()

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        SecureBackupEnterRecoveryKeyView(
            state = state,
            modifier = modifier,
            onSuccess = callback::onEnterRecoveryKeySuccess,
            onBackClick = ::navigateUp,
        )
    }
}

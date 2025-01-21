/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.setup.biometric

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
class SetupBiometricNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: SetupBiometricPresenter,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun onBiometricSetupDone()
    }

    private fun onSetupDone() {
        plugins<Callback>().forEach { it.onBiometricSetupDone() }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        LaunchedEffect(state.isBiometricSetupDone) {
            if (state.isBiometricSetupDone) {
                onSetupDone()
            }
        }
        SetupBiometricView(
            state = state,
            modifier = modifier
        )
    }
}

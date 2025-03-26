/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ftue.impl.sessionverification.choosemode

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.logout.api.direct.DirectLogoutView
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
class ChooseSelfVerificationModeNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: Presenter<ChooseSelfVerificationModeState>,
    private val directLogoutView: DirectLogoutView,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun onUseAnotherDevice()
        fun onUseRecoveryKey()
        fun onResetKey()
        fun onLearnMoreAboutEncryption()
    }

    private val callback = plugins<Callback>().first()

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()

        ChooseSelfVerificationModeView(
            state = state,
            onUseAnotherDevice = callback::onUseAnotherDevice,
            onUseRecoveryKey = callback::onUseRecoveryKey,
            onResetKey = callback::onResetKey,
            onLearnMore = callback::onLearnMoreAboutEncryption,
            modifier = modifier,
        )

        directLogoutView.Render(state = state.directLogoutState)
    }
}

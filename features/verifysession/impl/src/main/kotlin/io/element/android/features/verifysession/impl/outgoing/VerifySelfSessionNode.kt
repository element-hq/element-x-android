/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.outgoing

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.appconfig.LearnMoreConfig
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.verifysession.api.VerifySessionEntryPoint
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
class VerifySelfSessionNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: VerifySelfSessionPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    private val callback = plugins<VerifySessionEntryPoint.Callback>().first()

    private val inputs = inputs<VerifySessionEntryPoint.Params>()

    private val presenter = presenterFactory.create(
        showDeviceVerifiedScreen = inputs.showDeviceVerifiedScreen,
        verificationRequest = inputs.verificationRequest,
    )

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        VerifySelfSessionView(
            state = state,
            modifier = modifier,
            onLearnMoreClick = callback::onLearnMoreAboutEncryption,
            onFinish = callback::onDone,
            onBack = callback::onBack,
        )
    }
}

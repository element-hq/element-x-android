/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.outgoing

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.verifysession.api.OutgoingVerificationEntryPoint
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
@AssistedInject
class OutgoingVerificationNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: OutgoingVerificationPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    private val callback: OutgoingVerificationEntryPoint.Callback = callback()
    private val inputs = inputs<OutgoingVerificationEntryPoint.Params>()

    private val presenter = presenterFactory.create(
        showDeviceVerifiedScreen = inputs.showDeviceVerifiedScreen,
        verificationRequest = inputs.verificationRequest,
    )

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        OutgoingVerificationView(
            state = state,
            modifier = modifier,
            onLearnMoreClick = callback::navigateToLearnMoreAboutEncryption,
            onFinish = callback::onDone,
            onBack = callback::onBack,
        )
    }
}

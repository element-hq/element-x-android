/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.reset.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.logout.api.direct.DirectLogoutView
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
@AssistedInject
class ResetIdentityRootNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: ResetIdentityRootPresenter.Factory,
    private val directLogoutView: DirectLogoutView,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(val hasConfirmationOptions: Boolean) : NodeInputs

    interface Callback : Plugin {
        fun onContinue()
    }

    private val callback: Callback = callback()
    private val inputs: Inputs = inputs()
    private val presenter = presenterFactory.create(inputs.hasConfirmationOptions)

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        ResetIdentityRootView(
            modifier = modifier,
            state = state,
            onContinue = callback::onContinue,
            onBack = ::navigateUp,
        )
        directLogoutView.Render(state = state.directLogoutState)
    }
}

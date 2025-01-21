/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.signedout.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.core.SessionId

@ContributesNode(AppScope::class)
class SignedOutNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: SignedOutPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(
        val sessionId: SessionId,
    ) : NodeInputs

    private val inputs: Inputs = inputs()
    private val presenter = presenterFactory.create(inputs.sessionId.value)

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        SignedOutView(
            state = state,
            modifier = modifier
        )
    }
}

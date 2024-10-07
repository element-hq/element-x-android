/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.report

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
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId

@ContributesNode(RoomScope::class)
class ReportMessageNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: ReportMessagePresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(
        val eventId: EventId,
        val senderId: UserId,
    ) : NodeInputs

    private val inputs = inputs<Inputs>()

    private val presenter = presenterFactory.create(
        ReportMessagePresenter.Inputs(inputs.eventId, inputs.senderId)
    )

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        ReportMessageView(
            state = state,
            onBackClick = ::navigateUp,
            modifier = modifier
        )
    }
}

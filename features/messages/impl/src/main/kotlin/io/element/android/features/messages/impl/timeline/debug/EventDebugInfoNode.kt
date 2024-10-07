/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.debug

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
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo

@ContributesNode(RoomScope::class)
class EventDebugInfoNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(
        val eventId: EventId?,
        val timelineItemDebugInfo: TimelineItemDebugInfo,
    ) : NodeInputs

    private val inputs = inputs<Inputs>()

    private fun onBackClick() {
        navigateUp()
    }

    @Composable
    override fun View(modifier: Modifier) = with(inputs) {
        EventDebugInfoView(
            eventId = eventId,
            model = timelineItemDebugInfo.model,
            originalJson = timelineItemDebugInfo.originalJson,
            latestEditedJson = timelineItemDebugInfo.latestEditedJson,
            onBackClick = ::onBackClick
        )
    }
}

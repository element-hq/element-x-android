/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.reactionsummary

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.messages.impl.timeline.aTimelineItemReactions
import io.element.android.libraries.matrix.api.core.EventId

open class ReactionSummaryStateProvider : PreviewParameterProvider<ReactionSummaryState> {
    override val values = sequenceOf(aReactionSummaryState())
}

fun aReactionSummaryState(): ReactionSummaryState {
    val reactions = aTimelineItemReactions(8, true).reactions
    return ReactionSummaryState(
        target = ReactionSummaryState.Summary(
            reactions = reactions,
            selectedKey = reactions[0].key,
            selectedEventId = EventId("$1234"),
        ),
        eventSink = {}
    )
}

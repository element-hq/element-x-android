/*
 * Copyright 2026 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import io.element.android.compound.R
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.canBeForwarded
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableSet

/** Maximum number of messages that can be selected at once. */
const val MAX_SELECTION_COUNT = 10

@Immutable
sealed interface SelectionState {
    /** Selection mode is not active. */
    data object Disabled : SelectionState

    /** Selection mode is active for [action], with the given selection. */
    data class Active(
        val action: SelectionAction,
        val selectedEventIds: ImmutableSet<EventId>,
    ) : SelectionState
}

/**
 * An action which can be performed on several timeline events at once, and which selection mode can be entered for.
 */
enum class SelectionAction(@field:StringRes val titleRes: Int, @field:DrawableRes val iconRes: Int) {
    Forward(CommonStrings.action_forward, R.drawable.ic_compound_forward),
}

/**
 * Whether [event] can be part of a selection made for this action.
 * Each action uses the same criteria as the single event action it mirrors.
 */
fun SelectionAction.canApplyTo(event: TimelineItem.Event): Boolean = when (this) {
    SelectionAction.Forward -> event.isRemote && event.content.canBeForwarded()
}

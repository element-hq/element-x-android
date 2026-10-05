/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model

/**
 * Row-scoped selection state passed to [io.element.android.features.messages.impl.timeline.components.TimelineItemRow].
 *
 * @param isSelectionModeActive whether the timeline is currently in selection mode. Drives
 *   behaviour (e.g. the whole-row click target) and must reflect the real mode, not [progress].
 * @param isSelected whether this specific row's event is part of the current selection.
 * @param canBeSelected whether this specific row's event can be selected for the action selection mode was entered for.
 *   Rows which cannot be selected render no indicator, and their content does not slide.
 * @param progress animated 0f..1f value driving the visuals (content slide + indicator slide-in).
 *   Animated once in TimelineView and shared by every row. Differs from [isSelectionModeActive]
 *   during the enter/exit animation.
 */
data class TimelineItemSelectionData(
    val isSelectionModeActive: Boolean = false,
    val isSelected: Boolean = false,
    val canBeSelected: Boolean = false,
    val progress: Float = 0f,
)

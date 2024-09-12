/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

enum class TimestampPosition {
    /**
     * Timestamp should overlay the timeline event content (eg. image).
     */
    Overlay,

    /**
     * Timestamp should be aligned with the timeline event content if this is possible (eg. text).
     */
    Aligned,

    /**
     * Timestamp should always be rendered below the timeline event content (eg. poll).
     */
    Below;

    companion object {
        /**
         * Default timestamp position for timeline event contents.
         */
        val Default: TimestampPosition = Aligned
    }
}

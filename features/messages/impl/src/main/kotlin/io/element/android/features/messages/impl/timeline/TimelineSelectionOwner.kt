/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

/**
 * Implemented by the nodes rendering a timeline which supports selection mode, so that their parent
 * can exit the selection mode once a flow started on their behalf, i.e. forwarding, is done.
 */
interface TimelineSelectionOwner {
    fun exitTimelineSelection()
}

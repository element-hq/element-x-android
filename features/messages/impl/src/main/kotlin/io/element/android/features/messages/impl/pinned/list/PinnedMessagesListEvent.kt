/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.pinned.list

import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.matrix.api.core.ThreadId

sealed interface PinnedMessagesListEvent {
    data class HandleAction(val action: TimelineItemAction, val event: TimelineItem.Event) : PinnedMessagesListEvent
    data class OpenThread(val threadRootId: ThreadId) : PinnedMessagesListEvent
}

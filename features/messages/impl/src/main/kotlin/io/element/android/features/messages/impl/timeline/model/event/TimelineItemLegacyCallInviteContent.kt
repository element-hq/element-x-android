/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import io.element.android.libraries.matrix.api.timeline.item.event.EventType

data object TimelineItemLegacyCallInviteContent : TimelineItemEventContent {
    override val type: String
        get() = EventType.CALL_INVITE
}

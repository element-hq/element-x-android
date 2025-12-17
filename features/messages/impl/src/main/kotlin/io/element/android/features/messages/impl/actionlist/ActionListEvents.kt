/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.actionlist

import io.element.android.features.messages.impl.UserEventPermissions
import io.element.android.features.messages.impl.timeline.model.TimelineItem

sealed interface ActionListEvents {
    data object Clear : ActionListEvents
    data class ComputeForMessage(
        val event: TimelineItem.Event,
        val userEventPermissions: UserEventPermissions,
    ) : ActionListEvents
}

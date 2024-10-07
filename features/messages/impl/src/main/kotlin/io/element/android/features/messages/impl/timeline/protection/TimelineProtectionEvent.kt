/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.protection

import io.element.android.libraries.matrix.api.core.EventId

sealed interface TimelineProtectionEvent {
    data class ShowContent(val eventId: EventId?) : TimelineProtectionEvent
}

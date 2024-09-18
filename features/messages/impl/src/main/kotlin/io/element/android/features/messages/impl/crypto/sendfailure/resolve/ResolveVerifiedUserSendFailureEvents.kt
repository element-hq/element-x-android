/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.sendfailure.resolve

import io.element.android.features.messages.impl.timeline.model.TimelineItem

sealed interface ResolveVerifiedUserSendFailureEvents {
    data class ComputeForMessage(
        val messageEvent: TimelineItem.Event,
    ) : ResolveVerifiedUserSendFailureEvents

    data object ResolveAndResend : ResolveVerifiedUserSendFailureEvents
    data object Retry : ResolveVerifiedUserSendFailureEvents
    data object Dismiss : ResolveVerifiedUserSendFailureEvents
}

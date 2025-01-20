/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.data

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId

fun aKnockRequestPresentable(
    eventId: EventId = EventId("\$eventId"),
    userId: UserId = UserId("@jacob_ross:example.com"),
    displayName: String? = "Jacob Ross",
    avatarUrl: String? = null,
    reason: String? = "Hi, I would like to get access to this room please.",
    formattedDate: String? = "20 Nov 2024",
) = object : KnockRequestPresentable {
    override val eventId: EventId = eventId
    override val userId: UserId = userId
    override val displayName: String? = displayName
    override val avatarUrl: String? = avatarUrl
    override val reason: String? = reason
    override val formattedDate: String? = formattedDate
}

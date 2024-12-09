/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl

import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.UserId

data class KnockRequest(
    val userId: UserId,
    val displayName: String?,
    val avatarUrl: String?,
    val reason: String?,
    val formattedDate: String?,
)

fun KnockRequest.getAvatarData(size: AvatarSize) = AvatarData(
    id = userId.value,
    name = displayName,
    url = avatarUrl,
    size = size,
)

fun KnockRequest.getBestName(): String {
    return displayName?.takeIf { it.isNotEmpty() } ?: userId.value
}

fun aKnockRequest(
    userId: UserId = UserId("@jacob_ross:example.com"),
    displayName: String? = "Jacob Ross",
    avatarUrl: String? = null,
    reason: String? = "Hi, I would like to get access to this room please.",
    formattedDate: String = "20 Nov 2024",
) = KnockRequest(
    userId = userId,
    displayName = displayName,
    avatarUrl = avatarUrl,
    reason = reason,
    formattedDate = formattedDate,
)

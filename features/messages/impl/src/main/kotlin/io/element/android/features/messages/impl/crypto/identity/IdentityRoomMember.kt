/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.identity

import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.api.core.UserId

data class IdentityRoomMember(
    val userId: UserId,
    val displayNameOrDefault: String,
    val avatarData: AvatarData,
)

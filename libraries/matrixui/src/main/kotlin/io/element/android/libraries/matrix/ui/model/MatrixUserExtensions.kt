/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.model

import fr.gouv.tchap.android.libraries.matrix.api.core.toDisplayName
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.user.MatrixUser

fun MatrixUser.getAvatarData(size: AvatarSize) = AvatarData(
    id = userId.value,
    name = displayName,
    url = avatarUrl,
    size = size,
)

fun MatrixUser.getBestName(): String {
    return displayName?.takeIf { it.isNotEmpty() } ?: userId.toDisplayName() // TCHAP hide the Matrix Id
}

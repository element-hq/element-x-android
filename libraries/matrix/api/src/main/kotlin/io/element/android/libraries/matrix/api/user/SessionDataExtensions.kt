/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.user

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.sessionstorage.api.SessionData

/**
 * Create a [MatrixUser] from the profile data stored locally for the session.
 * The avatar thumbnail is used as a fallback when the avatar cannot be loaded,
 * for instance for a session which is not the current one.
 */
fun SessionData.toMatrixUser() = MatrixUser(
    userId = UserId(userId),
    displayName = userDisplayName,
    avatarUrl = userAvatarUrl,
    avatarThumbnail = userAvatarData,
)

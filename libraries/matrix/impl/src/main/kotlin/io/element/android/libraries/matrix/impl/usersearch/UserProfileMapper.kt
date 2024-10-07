/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.usersearch

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import org.matrix.rustcomponents.sdk.UserProfile

object UserProfileMapper {
    fun map(userProfile: UserProfile): MatrixUser =
        MatrixUser(
            userId = UserId(userProfile.userId),
            displayName = userProfile.displayName,
            avatarUrl = userProfile.avatarUrl,
        )
}

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.usersearch

import io.element.android.libraries.matrix.api.user.MatrixSearchUserResults
import kotlinx.collections.immutable.toImmutableList
import org.matrix.rustcomponents.sdk.SearchUsersResults

object UserSearchResultMapper {
    fun map(result: SearchUsersResults): MatrixSearchUserResults {
        return MatrixSearchUserResults(
            results = result.results.map(UserProfileMapper::map).toImmutableList(),
            limited = result.limited,
        )
    }
}

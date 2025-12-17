/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.usersearch

import io.element.android.libraries.matrix.api.user.MatrixSearchUserResults
import io.element.android.libraries.matrix.impl.mapper.map
import kotlinx.collections.immutable.toImmutableList
import org.matrix.rustcomponents.sdk.SearchUsersResults

object UserSearchResultMapper {
    fun map(result: SearchUsersResults): MatrixSearchUserResults {
        return MatrixSearchUserResults(
            results = result.results
                .map { userProfile -> userProfile.map() }
                .toImmutableList(),
            limited = result.limited,
        )
    }
}

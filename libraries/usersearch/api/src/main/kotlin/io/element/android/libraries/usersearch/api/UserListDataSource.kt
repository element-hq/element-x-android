/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.usersearch.api

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser

/**
 * Low-level access to the homeserver's user directory; see [UserRepository] for the search used by the UI.
 */
interface UserListDataSource {
    /**
     * Searches the user directory, returning an empty list rather than failing when the request does not succeed.
     * TODO should probably have a flow
     *
     * @param query the text to look for in user ids and display names.
     * @param count the maximum number of results to return.
     */
    suspend fun search(query: String, count: Long): List<MatrixUser>

    /**
     * Fetches one user's profile.
     *
     * @param userId the user whose profile is requested.
     * @return the profile, or `null` when it could not be retrieved.
     */
    suspend fun getProfile(userId: UserId): MatrixUser?
}

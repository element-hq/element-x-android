/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.usersearch.api

import kotlinx.coroutines.flow.Flow

/**
 * Searches for users to start a conversation with or invite, combining the homeserver directory with what the app already knows.
 */
interface UserRepository {
    /**
     * Runs a search and emits its progress, so the UI can show local results before the directory answers.
     * A query that is a well-formed user id other than the current user's also yields that user, even when the directory does not know them.
     *
     * @param query the text typed by the user.
     */
    fun search(query: String): Flow<UserSearchResultState>
}

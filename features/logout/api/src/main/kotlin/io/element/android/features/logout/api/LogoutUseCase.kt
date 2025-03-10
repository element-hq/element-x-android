/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.logout.api

/**
 * Used to trigger a log out of the current user from any part of the app.
 */
interface LogoutUseCase {
    /**
     * Log out the current user and then perform any needed cleanup tasks.
     * @param ignoreSdkError if true, the SDK error will be ignored and the user will be logged out anyway.
     */
    suspend fun logout(ignoreSdkError: Boolean)

    interface Factory {
        fun create(sessionId: String): LogoutUseCase
    }
}

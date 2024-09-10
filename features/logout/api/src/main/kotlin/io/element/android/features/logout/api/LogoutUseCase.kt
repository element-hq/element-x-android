/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.logout.api

/**
 * Used to trigger a log out of the current user from any part of the app.
 */
interface LogoutUseCase {
    /**
     * Log out the current user and then perform any needed cleanup tasks.
     * @param ignoreSdkError if true, the SDK error will be ignored and the user will be logged out anyway.
     * @return an optional URL. When the URL is there, it should be presented to the user after logout for
     * Relying Party (RP) initiated logout on their account page.
     */
    suspend fun logout(ignoreSdkError: Boolean): String?

    interface Factory {
        fun create(sessionId: String): LogoutUseCase
    }
}

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.permissions.api

import kotlinx.coroutines.flow.Flow

/**
 * Remembers what the app has already asked the user about, since Android itself does not let an app tell
 * "never asked" apart from "asked and denied".
 */
interface PermissionsStore {
    /**
     * @param permission the Android permission name.
     * @param value true once the user has refused this permission.
     */
    suspend fun setPermissionDenied(permission: String, value: Boolean)

    /**
     * Whether the user has refused this permission before.
     *
     * @param permission the Android permission name.
     */
    fun isPermissionDenied(permission: String): Flow<Boolean>

    /**
     * @param permission the Android permission name.
     * @param value true once the system dialog for this permission has been shown.
     */
    suspend fun setPermissionAsked(permission: String, value: Boolean)

    /**
     * Whether the system dialog for this permission has already been shown, which decides whether the app may show a rationale instead.
     *
     * @param permission the Android permission name.
     */
    fun isPermissionAsked(permission: String): Flow<Boolean>

    /**
     * Forgets what was recorded about one permission, so the app behaves as if it had never been requested.
     *
     * @param permission the Android permission name.
     */
    suspend fun resetPermission(permission: String)

    /** Forgets every recorded permission; exposed for debugging from the developer options. */
    suspend fun resetStore()
}

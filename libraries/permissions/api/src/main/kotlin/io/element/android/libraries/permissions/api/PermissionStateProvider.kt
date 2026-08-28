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
 * Answers permission questions by combining the live system state with what [PermissionsStore] remembers about past requests.
 */
interface PermissionStateProvider {
    /**
     * Whether the permission is granted right now, read from the system rather than from the store.
     *
     * @param permission the Android permission name.
     */
    fun isPermissionGranted(permission: String): Boolean

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
     * Whether the system dialog for this permission has already been shown.
     *
     * @param permission the Android permission name.
     */
    fun isPermissionAsked(permission: String): Flow<Boolean>
}

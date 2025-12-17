/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.permissions.api

import kotlinx.coroutines.flow.Flow

interface PermissionStateProvider {
    fun isPermissionGranted(permission: String): Boolean
    suspend fun setPermissionDenied(permission: String, value: Boolean)
    fun isPermissionDenied(permission: String): Flow<Boolean>

    suspend fun setPermissionAsked(permission: String, value: Boolean)
    fun isPermissionAsked(permission: String): Flow<Boolean>
}

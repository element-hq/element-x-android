/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.permissions.test

import io.element.android.libraries.permissions.api.PermissionStateProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePermissionStateProvider(
    private var permissionGranted: Boolean = true,
    permissionDenied: Boolean = false,
    permissionAsked: Boolean = false,
) : PermissionStateProvider {
    private val permissionDeniedFlow = MutableStateFlow(permissionDenied)
    private val permissionAskedFlow = MutableStateFlow(permissionAsked)

    fun setPermissionGranted() {
        permissionGranted = true
    }

    override fun isPermissionGranted(permission: String): Boolean = permissionGranted

    override suspend fun setPermissionDenied(permission: String, value: Boolean) {
        permissionDeniedFlow.value = value
    }

    override fun isPermissionDenied(permission: String): Flow<Boolean> = permissionDeniedFlow

    override suspend fun setPermissionAsked(permission: String, value: Boolean) {
        permissionAskedFlow.value = value
    }

    override fun isPermissionAsked(permission: String): Flow<Boolean> = permissionAskedFlow
}

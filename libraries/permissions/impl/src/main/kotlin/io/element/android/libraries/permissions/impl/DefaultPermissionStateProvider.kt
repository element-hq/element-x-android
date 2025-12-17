/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.permissions.impl

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.permissions.api.PermissionStateProvider
import io.element.android.libraries.permissions.api.PermissionsStore
import kotlinx.coroutines.flow.Flow

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultPermissionStateProvider(
    @ApplicationContext private val context: Context,
    private val permissionsStore: PermissionsStore,
) : PermissionStateProvider {
    override fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission,
        ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun setPermissionDenied(permission: String, value: Boolean) = permissionsStore.setPermissionDenied(permission, value)

    override fun isPermissionDenied(permission: String): Flow<Boolean> = permissionsStore.isPermissionDenied(permission)

    override suspend fun setPermissionAsked(permission: String, value: Boolean) = permissionsStore.setPermissionAsked(permission, value)

    override fun isPermissionAsked(permission: String): Flow<Boolean> = permissionsStore.isPermissionAsked(permission)
}

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.permissions.impl

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.permissions.api.PermissionStateProvider
import io.element.android.libraries.permissions.api.PermissionsStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultPermissionStateProvider @Inject constructor(
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

    override suspend fun resetPermission(permission: String) = permissionsStore.resetPermission(permission)
}

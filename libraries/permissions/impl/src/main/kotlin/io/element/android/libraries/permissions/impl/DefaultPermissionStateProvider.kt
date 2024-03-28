/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

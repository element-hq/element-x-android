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

import io.element.android.libraries.permissions.api.PermissionStateProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePermissionStateProvider(
    private var permissionGranted: Boolean = true,
    permissionDenied: Boolean = false,
    permissionAsked: Boolean = false,
    private val resetPermissionLambda: (String) -> Unit = {},
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

    override suspend fun resetPermission(permission: String) {
        setPermissionAsked(permission, false)
        setPermissionDenied(permission, false)
        resetPermissionLambda(permission)
    }
}

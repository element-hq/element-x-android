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

package io.element.android.libraries.permissions.api

import kotlinx.coroutines.flow.Flow

interface PermissionStateProvider {
    fun isPermissionGranted(permission: String): Boolean
    suspend fun setPermissionDenied(permission: String, value: Boolean)
    fun isPermissionDenied(permission: String): Flow<Boolean>

    suspend fun setPermissionAsked(permission: String, value: Boolean)
    fun isPermissionAsked(permission: String): Flow<Boolean>

    suspend fun resetPermission(permission: String)
}

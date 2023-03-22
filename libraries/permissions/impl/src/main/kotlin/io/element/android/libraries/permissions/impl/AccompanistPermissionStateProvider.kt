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

@file:OptIn(ExperimentalPermissionsApi::class)

package io.element.android.libraries.permissions.impl

import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

interface PermissionStateProvider {
    @Composable
    fun provide(permission: String, onPermissionResult: (Boolean) -> Unit): PermissionState
}

@ContributesBinding(AppScope::class)
class AccompanistPermissionStateProvider @Inject constructor() : PermissionStateProvider {
    @Composable
    override fun provide(permission: String, onPermissionResult: (Boolean) -> Unit): PermissionState {
        return rememberPermissionState(
            permission = permission,
            onPermissionResult = onPermissionResult
        )
    }
}

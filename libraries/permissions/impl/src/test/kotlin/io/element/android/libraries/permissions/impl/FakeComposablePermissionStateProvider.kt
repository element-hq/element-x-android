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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus

class FakeComposablePermissionStateProvider(
    private val permissionState: FakePermissionState
) : ComposablePermissionStateProvider {
    private lateinit var onPermissionResult: (Boolean) -> Unit

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    override fun provide(permission: String, onPermissionResult: (Boolean) -> Unit): PermissionState {
        this.onPermissionResult = onPermissionResult
        return permissionState
    }

    fun userGiveAnswer(answer: Boolean, firstTime: Boolean) {
        onPermissionResult.invoke(answer)
        permissionState.givenPermissionStatus(answer, firstTime)
    }
}

@Stable
class FakePermissionState(
    override val permission: String,
    initialStatus: PermissionStatus,
) : PermissionState {

    override var status: PermissionStatus by mutableStateOf(initialStatus)

    var launchPermissionRequestCalled = false
        private set

    override fun launchPermissionRequest() {
        launchPermissionRequestCalled = true
    }

    fun givenPermissionStatus(hasPermission: Boolean, shouldShowRationale: Boolean) {
        status = if (hasPermission) PermissionStatus.Granted else PermissionStatus.Denied(shouldShowRationale)
    }
}

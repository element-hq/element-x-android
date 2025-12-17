/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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

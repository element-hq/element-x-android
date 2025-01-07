/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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

interface ComposablePermissionStateProvider {
    @Composable
    fun provide(permission: String, onPermissionResult: (Boolean) -> Unit): PermissionState
}

@ContributesBinding(AppScope::class)
class AccompanistPermissionStateProvider @Inject constructor() : ComposablePermissionStateProvider {
    @Composable
    override fun provide(permission: String, onPermissionResult: (Boolean) -> Unit): PermissionState {
        return rememberPermissionState(
            permission = permission,
            onPermissionResult = onPermissionResult
        )
    }
}

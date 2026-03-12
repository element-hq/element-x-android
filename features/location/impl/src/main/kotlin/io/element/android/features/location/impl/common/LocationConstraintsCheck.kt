/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common

import io.element.android.features.location.impl.common.actions.LocationActions
import io.element.android.features.location.impl.common.permissions.PermissionsState
import io.element.android.features.location.impl.common.ui.LocationConstraintsDialogState

sealed interface LocationConstraintsCheckResult {
    data object Success : LocationConstraintsCheckResult
    data object PermissionRationale : LocationConstraintsCheckResult
    data object PermissionDenied : LocationConstraintsCheckResult
    data object LocationServiceDisabled : LocationConstraintsCheckResult
}

fun checkLocationConstraints(
    permissionsState: PermissionsState,
    locationActions: LocationActions,
): LocationConstraintsCheckResult {
    return when {
        permissionsState.isAnyGranted -> {
            if (locationActions.isLocationEnabled()) {
                LocationConstraintsCheckResult.Success
            } else {
                LocationConstraintsCheckResult.LocationServiceDisabled
            }
        }
        permissionsState.shouldShowRationale -> LocationConstraintsCheckResult.PermissionRationale
        else -> LocationConstraintsCheckResult.PermissionDenied
    }
}

fun LocationConstraintsCheckResult.toDialogState(): LocationConstraintsDialogState {
    return when (this) {
        LocationConstraintsCheckResult.Success -> LocationConstraintsDialogState.None
        LocationConstraintsCheckResult.PermissionRationale -> LocationConstraintsDialogState.PermissionRationale
        LocationConstraintsCheckResult.PermissionDenied -> LocationConstraintsDialogState.PermissionDenied
        LocationConstraintsCheckResult.LocationServiceDisabled -> LocationConstraintsDialogState.LocationServiceDisabled
    }
}

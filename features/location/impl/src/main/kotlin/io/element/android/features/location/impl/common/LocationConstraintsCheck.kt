/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common

import io.element.android.features.location.impl.common.actions.LocationActions
import io.element.android.features.location.impl.common.permissions.PermissionsState
import io.element.android.features.location.impl.common.ui.LocationConstraintsDialogState

sealed interface LocationConstraintsCheck {
    data object Success : LocationConstraintsCheck
    data object PermissionRationale : LocationConstraintsCheck
    data object PermissionDenied : LocationConstraintsCheck
    data object LocationServiceDisabled : LocationConstraintsCheck
}

fun checkLocationConstraints(
    permissionsState: PermissionsState,
    locationActions: LocationActions,
): LocationConstraintsCheck {
    return when {
        permissionsState.isAnyGranted -> {
            if (locationActions.isLocationEnabled()) {
                LocationConstraintsCheck.Success
            } else {
                LocationConstraintsCheck.LocationServiceDisabled
            }
        }
        permissionsState.shouldShowRationale -> LocationConstraintsCheck.PermissionRationale
        else -> LocationConstraintsCheck.PermissionDenied
    }
}

fun LocationConstraintsCheck.toDialogState(): LocationConstraintsDialogState {
    return when (this) {
        LocationConstraintsCheck.Success -> LocationConstraintsDialogState.None
        LocationConstraintsCheck.PermissionRationale -> LocationConstraintsDialogState.PermissionRationale
        LocationConstraintsCheck.PermissionDenied -> LocationConstraintsDialogState.PermissionDenied
        LocationConstraintsCheck.LocationServiceDisabled -> LocationConstraintsDialogState.LocationServiceDisabled
    }
}

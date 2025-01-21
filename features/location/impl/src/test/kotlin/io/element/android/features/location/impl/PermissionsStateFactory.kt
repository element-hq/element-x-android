/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl

import io.element.android.features.location.impl.common.permissions.PermissionsState

fun aPermissionsState(
    permissions: PermissionsState.Permissions = PermissionsState.Permissions.NoneGranted,
    shouldShowRationale: Boolean = false,
): PermissionsState {
    return PermissionsState(
        permissions = permissions,
        shouldShowRationale = shouldShowRationale,
        eventSink = {},
    )
}

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.oidc

import io.element.android.libraries.matrix.api.core.DeviceId

sealed interface AccountManagementAction {
    data object Profile : AccountManagementAction
    data object SessionsList : AccountManagementAction
    data class SessionView(val deviceId: DeviceId) : AccountManagementAction
    data class SessionEnd(val deviceId: DeviceId) : AccountManagementAction
}

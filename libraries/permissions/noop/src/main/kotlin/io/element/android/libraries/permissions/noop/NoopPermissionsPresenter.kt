/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.permissions.noop

import androidx.compose.runtime.Composable
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.permissions.api.PermissionsState

class NoopPermissionsPresenter(
    private val isGranted: Boolean = false,
) : PermissionsPresenter {
    @Composable
    override fun present(): PermissionsState {
        return PermissionsState(
            permission = "",
            permissionGranted = isGranted,
            shouldShowRationale = false,
            showDialog = false,
            permissionAlreadyAsked = false,
            permissionAlreadyDenied = false,
            eventSink = {},
        )
    }
}

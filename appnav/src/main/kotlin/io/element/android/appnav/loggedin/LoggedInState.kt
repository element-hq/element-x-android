/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.loggedin

import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.permissions.api.localnetwork.LocalNetworkPermissionDialog

enum class SyncIndicatorState {
    Hidden,
    Syncing,
    ServerUnreachable,
}

data class LoggedInState(
    val syncIndicatorState: SyncIndicatorState,
    val pusherRegistrationState: AsyncData<Unit>,
    val ignoreRegistrationError: Boolean,
    val forceNativeSlidingSyncMigration: Boolean,
    val appName: String,
    val localNetworkPermissionDialog: LocalNetworkPermissionDialog,
    val eventSink: (LoggedInEvent) -> Unit,
)

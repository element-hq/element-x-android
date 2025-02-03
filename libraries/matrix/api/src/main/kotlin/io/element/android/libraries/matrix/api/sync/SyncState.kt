/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.sync

enum class SyncState {
    Idle,
    Running,
    Error,
    Terminated,
    Offline,
}

fun SyncState.isConnected() = when (this) {
    SyncState.Idle,
    SyncState.Running,
    SyncState.Error,
    SyncState.Terminated -> true
    SyncState.Offline -> false
}

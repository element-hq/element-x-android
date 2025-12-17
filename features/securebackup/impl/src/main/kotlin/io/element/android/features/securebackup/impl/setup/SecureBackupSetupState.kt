/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.setup

import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyViewState

data class SecureBackupSetupState(
    val isChangeRecoveryKeyUserStory: Boolean,
    val recoveryKeyViewState: RecoveryKeyViewState,
    val showSaveConfirmationDialog: Boolean,
    val setupState: SetupState,
    val eventSink: (SecureBackupSetupEvents) -> Unit
)

sealed interface SetupState {
    data object Init : SetupState
    data object Creating : SetupState
    data class Created(val formattedRecoveryKey: String) : SetupState
    data class CreatedAndSaved(val formattedRecoveryKey: String) : SetupState
    data class Error(val exception: Exception) : SetupState
}

fun SetupState.recoveryKey(): String? = when (this) {
    is SetupState.Created -> formattedRecoveryKey
    is SetupState.CreatedAndSaved -> formattedRecoveryKey
    else -> null
}

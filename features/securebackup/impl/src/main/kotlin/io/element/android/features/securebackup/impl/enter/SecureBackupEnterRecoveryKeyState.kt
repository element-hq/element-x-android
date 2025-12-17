/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.enter

import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyViewState
import io.element.android.libraries.architecture.AsyncAction

data class SecureBackupEnterRecoveryKeyState(
    val recoveryKeyViewState: RecoveryKeyViewState,
    val isSubmitEnabled: Boolean,
    val submitAction: AsyncAction<Unit>,
    val eventSink: (SecureBackupEnterRecoveryKeyEvents) -> Unit
)

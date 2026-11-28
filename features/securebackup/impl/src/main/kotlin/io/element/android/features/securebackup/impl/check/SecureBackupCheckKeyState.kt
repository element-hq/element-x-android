/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.check

import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyViewState

data class SecureBackupCheckKeyState(
    val recoveryKeyViewState: RecoveryKeyViewState,
    val isSubmitEnabled: Boolean,
    /** Whether the key that was entered turned out not to be the right one. */
    val isKeyIncorrect: Boolean,
    /** Whether the key that was entered is right, so the screen is only showing that. */
    val isCompleted: Boolean,
    val eventSink: (SecureBackupCheckKeyEvent) -> Unit,
)

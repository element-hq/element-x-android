/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.setup.views

data class RecoveryKeyViewState(
    val recoveryKeyUserStory: RecoveryKeyUserStory,
    val formattedRecoveryKey: String?,
    val displayTextFieldContents: Boolean,
    val inProgress: Boolean,
)

enum class RecoveryKeyUserStory {
    Setup,
    Change,
    Enter,
}

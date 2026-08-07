/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.classic.missingkeybackup

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class MissingKeyBackupStateProvider : PreviewParameterProvider<MissingKeyBackupState> {
    override val values: Sequence<MissingKeyBackupState>
        get() = sequenceOf(
            aMissingKeyBackupState(),
            // Add other state here
        )
}

fun aMissingKeyBackupState(
    appName: String = "AppName",
) = MissingKeyBackupState(
    appName = appName,
)

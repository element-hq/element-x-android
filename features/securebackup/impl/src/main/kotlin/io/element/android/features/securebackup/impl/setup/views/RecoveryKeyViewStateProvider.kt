/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.setup.views

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class RecoveryKeyViewStateProvider : PreviewParameterProvider<RecoveryKeyViewState> {
    override val values: Sequence<RecoveryKeyViewState>
        get() = sequenceOf(RecoveryKeyUserStory.Setup, RecoveryKeyUserStory.Change, RecoveryKeyUserStory.Enter)
            .flatMap {
                sequenceOf(
                    aRecoveryKeyViewState(recoveryKeyUserStory = it),
                    aRecoveryKeyViewState(recoveryKeyUserStory = it, inProgress = true),
                    aRecoveryKeyViewState(recoveryKeyUserStory = it, formattedRecoveryKey = aFormattedRecoveryKey()),
                    aRecoveryKeyViewState(recoveryKeyUserStory = it, formattedRecoveryKey = aFormattedRecoveryKey(), inProgress = true),
                )
            } + sequenceOf(
            aRecoveryKeyViewState(recoveryKeyUserStory = RecoveryKeyUserStory.Enter, formattedRecoveryKey = aFormattedRecoveryKey().replace(" ", "")),
            aRecoveryKeyViewState(recoveryKeyUserStory = RecoveryKeyUserStory.Enter, formattedRecoveryKey = "This is a passphrase with spaces"),
            aRecoveryKeyViewState(
                recoveryKeyUserStory = RecoveryKeyUserStory.Enter,
                formattedRecoveryKey = aFormattedRecoveryKey().replace(" ", ""),
                displayTextFieldContents = false
            ),
        )
}

fun aRecoveryKeyViewState(
    recoveryKeyUserStory: RecoveryKeyUserStory = RecoveryKeyUserStory.Setup,
    formattedRecoveryKey: String? = null,
    inProgress: Boolean = false,
    displayTextFieldContents: Boolean = true,
) = RecoveryKeyViewState(
    recoveryKeyUserStory = recoveryKeyUserStory,
    formattedRecoveryKey = formattedRecoveryKey,
    displayTextFieldContents = displayTextFieldContents,
    inProgress = inProgress,
)

internal fun aFormattedRecoveryKey(): String {
    return "Estm dfyU adhD h8y6 Estm dfyU adhD h8y6 Estm dfyU adhD h8y6"
}

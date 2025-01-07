/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
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
        )
}

fun aRecoveryKeyViewState(
    recoveryKeyUserStory: RecoveryKeyUserStory = RecoveryKeyUserStory.Setup,
    formattedRecoveryKey: String? = null,
    inProgress: Boolean = false,
) = RecoveryKeyViewState(
    recoveryKeyUserStory = recoveryKeyUserStory,
    formattedRecoveryKey = formattedRecoveryKey,
    inProgress = inProgress,
)

internal fun aFormattedRecoveryKey(): String {
    return "Estm dfyU adhD h8y6 Estm dfyU adhD h8y6 Estm dfyU adhD h8y6"
}

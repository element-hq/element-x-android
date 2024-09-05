/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

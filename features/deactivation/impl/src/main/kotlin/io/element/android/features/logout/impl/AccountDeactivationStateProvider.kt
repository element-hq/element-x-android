/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.logout.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction

open class AccountDeactivationStateProvider : PreviewParameterProvider<AccountDeactivationState> {
    private val filledForm = aDeactivateFormState(eraseData = true, password = "password")
    override val values: Sequence<AccountDeactivationState>
        get() = sequenceOf(
            anAccountDeactivationState(),
            anAccountDeactivationState(
                deactivateFormState = filledForm
            ),
            anAccountDeactivationState(
                deactivateFormState = filledForm,
                accountDeactivationAction = AsyncAction.ConfirmingNoParams,
            ),
            anAccountDeactivationState(
                deactivateFormState = filledForm,
                accountDeactivationAction = AsyncAction.Loading
            ),
            anAccountDeactivationState(
                deactivateFormState = filledForm,
                accountDeactivationAction = AsyncAction.Failure(Exception("Failed to deactivate account"))
            ),
        )
}

internal fun aDeactivateFormState(
    eraseData: Boolean = false,
    password: String = "",
) = DeactivateFormState(
    eraseData = eraseData,
    password = password,
)

internal fun anAccountDeactivationState(
    deactivateFormState: DeactivateFormState = aDeactivateFormState(),
    accountDeactivationAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (AccountDeactivationEvents) -> Unit = {},
) = AccountDeactivationState(
    deactivateFormState = deactivateFormState,
    accountDeactivationAction = accountDeactivationAction,
    eventSink = eventSink,
)

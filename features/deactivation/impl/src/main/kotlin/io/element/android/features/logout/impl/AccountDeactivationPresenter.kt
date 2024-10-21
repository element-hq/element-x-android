/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.logout.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.matrix.api.MatrixClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class AccountDeactivationPresenter @Inject constructor(
    private val matrixClient: MatrixClient,
) : Presenter<AccountDeactivationState> {
    @Composable
    override fun present(): AccountDeactivationState {
        val localCoroutineScope = rememberCoroutineScope()
        val action: MutableState<AsyncAction<Unit>> = remember {
            mutableStateOf(AsyncAction.Uninitialized)
        }

        val formState = remember { mutableStateOf(DeactivateFormState.Default) }

        fun handleEvents(event: AccountDeactivationEvents) {
            when (event) {
                is AccountDeactivationEvents.SetEraseData -> {
                    updateFormState(formState) {
                        copy(eraseData = event.eraseData)
                    }
                }
                is AccountDeactivationEvents.SetPassword -> {
                    updateFormState(formState) {
                        copy(password = event.password)
                    }
                }
                is AccountDeactivationEvents.DeactivateAccount ->
                    if (action.value.isConfirming() || event.isRetry) {
                        localCoroutineScope.deactivateAccount(
                            formState = formState.value,
                            action
                        )
                    } else {
                        action.value = AsyncAction.ConfirmingNoParams
                    }
                AccountDeactivationEvents.CloseDialogs -> {
                    action.value = AsyncAction.Uninitialized
                }
            }
        }

        return AccountDeactivationState(
            deactivateFormState = formState.value,
            accountDeactivationAction = action.value,
            eventSink = ::handleEvents
        )
    }

    private fun updateFormState(formState: MutableState<DeactivateFormState>, updateLambda: DeactivateFormState.() -> DeactivateFormState) {
        formState.value = updateLambda(formState.value)
    }

    private fun CoroutineScope.deactivateAccount(
        formState: DeactivateFormState,
        action: MutableState<AsyncAction<Unit>>,
    ) = launch {
        suspend {
            matrixClient.deactivateAccount(
                password = formState.password,
                eraseData = formState.eraseData,
            ).getOrThrow()
        }.runCatchingUpdatingState(action)
    }
}

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

package io.element.android.libraries.designsystem.components.async

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialogDefaults
import io.element.android.libraries.designsystem.components.dialogs.RetryDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

/**
 * Render an AsyncAction object.
 * - If Success, invoke the callback [onSuccess], only once.
 * - If Failure, display a dialog with the error, which can be transformed, using [errorMessage]. When
 * closed, [onErrorDismiss] will be invoked. If [onRetry] is not null, a retry button will be displayed.
 * - When loading, display a loading dialog using [progressDialog]. Pass empty lambda to disable.
 */
@Composable
fun <T> AsyncActionView(
    async: AsyncAction<T>,
    onSuccess: (T) -> Unit,
    onErrorDismiss: () -> Unit,
    confirmationDialog: @Composable () -> Unit = { },
    progressDialog: @Composable () -> Unit = { AsyncActionViewDefaults.ProgressDialog() },
    errorTitle: @Composable (Throwable) -> String = { ErrorDialogDefaults.title },
    errorMessage: @Composable (Throwable) -> String = { it.message ?: it.toString() },
    onRetry: (() -> Unit)? = null,
) {
    when (async) {
        AsyncAction.Uninitialized -> Unit
        AsyncAction.Confirming -> confirmationDialog()
        is AsyncAction.Loading -> progressDialog()
        is AsyncAction.Failure -> {
            if (onRetry == null) {
                ErrorDialog(
                    title = errorTitle(async.error),
                    content = errorMessage(async.error),
                    onDismiss = onErrorDismiss
                )
            } else {
                RetryDialog(
                    title = errorTitle(async.error),
                    content = errorMessage(async.error),
                    onDismiss = onErrorDismiss,
                    onRetry = onRetry,
                )
            }
        }
        is AsyncAction.Success -> {
            val latestOnSuccess by rememberUpdatedState(onSuccess)
            LaunchedEffect(async) {
                latestOnSuccess(async.data)
            }
        }
    }
}

object AsyncActionViewDefaults {
    @Composable
    fun ProgressDialog(progressText: String? = null) {
        ProgressDialog(
            text = progressText,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun AsyncActionViewPreview(
    @PreviewParameter(AsyncActionProvider::class) async: AsyncAction<Unit>,
) = ElementPreview {
    AsyncActionView(
        async = async,
        onSuccess = {},
        onErrorDismiss = {},
        confirmationDialog = {
            ConfirmationDialog(
                title = "Confirmation",
                content = "Are you sure?",
                onSubmitClicked = {},
                onDismiss = {},
            )
        },
    )
}

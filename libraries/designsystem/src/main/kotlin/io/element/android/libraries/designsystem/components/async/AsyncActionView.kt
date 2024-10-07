/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
@Suppress("ContentSlotReused") // False positive, the lambdas don't add composable views
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
                    onSubmit = onErrorDismiss
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
                onSubmitClick = {},
                onDismiss = {},
            )
        },
    )
}

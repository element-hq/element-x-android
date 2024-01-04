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
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialogDefaults
import io.element.android.libraries.designsystem.components.dialogs.RetryDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

/**
 * Render an Async object.
 * - If Success, invoke the callback [onSuccess], only once.
 * - If Failure, display a dialog with the error, which can be transformed, using [errorMessage]. When
 * closed, [onErrorDismiss] will be invoked. If [onRetry] is not null, a retry button will be displayed.
 * - When loading, display a loading dialog, if [showProgressDialog] is true, with on optional [progressText].
 */
@Composable
fun <T> AsyncView(
    async: AsyncData<T>,
    onSuccess: (T) -> Unit,
    onErrorDismiss: () -> Unit,
    showProgressDialog: Boolean = true,
    progressText: String? = null,
    errorTitle: @Composable (Throwable) -> String = { ErrorDialogDefaults.title },
    errorMessage: @Composable (Throwable) -> String = { it.message ?: it.toString() },
    onRetry: (() -> Unit)? = null,
) {
    AsyncView(
        async = async,
        onSuccess = onSuccess,
        onErrorDismiss = onErrorDismiss,
        progressDialog = {
            if (showProgressDialog) {
                AsyncViewDefaults.ProgressDialog(progressText)
            }
        },
        errorTitle = errorTitle,
        errorMessage = errorMessage,
        onRetry = onRetry,
    )
}

@Composable
fun <T> AsyncView(
    async: AsyncData<T>,
    onSuccess: (T) -> Unit,
    onErrorDismiss: () -> Unit,
    progressDialog: @Composable () -> Unit = { AsyncViewDefaults.ProgressDialog() },
    errorTitle: @Composable (Throwable) -> String = { ErrorDialogDefaults.title },
    errorMessage: @Composable (Throwable) -> String = { it.message ?: it.toString() },
    onRetry: (() -> Unit)? = null,
) {
    when (async) {
        AsyncData.Uninitialized -> Unit
        is AsyncData.Loading -> progressDialog()
        is AsyncData.Failure -> {
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
        is AsyncData.Success -> {
            LaunchedEffect(async) {
                onSuccess(async.data)
            }
        }
    }
}

object AsyncViewDefaults {
    @Composable
    fun ProgressDialog(progressText: String? = null) {
        ProgressDialog(
            text = progressText,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun AsyncViewPreview(
    @PreviewParameter(AsyncProvider::class) async: AsyncData<Unit>,
) = ElementPreview {
    AsyncView(
        async = async,
        onSuccess = {},
        onErrorDismiss = {},
    )
}

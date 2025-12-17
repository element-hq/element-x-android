/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.apperror.impl

import androidx.compose.runtime.Composable
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.services.apperror.api.AppErrorState
import io.element.android.services.apperror.api.aAppErrorState

@Composable
fun AppErrorView(
    state: AppErrorState,
) {
    if (state is AppErrorState.Error) {
        AppErrorViewContent(
            title = state.title,
            body = state.body,
            onDismiss = state.dismiss,
        )
    }
}

@Composable
private fun AppErrorViewContent(
    title: String,
    body: String,
    onDismiss: () -> Unit = { },
) {
    ErrorDialog(
        title = title,
        content = body,
        onSubmit = onDismiss,
    )
}

@PreviewsDayNight
@Composable
internal fun AppErrorViewPreview() = ElementPreview {
    AppErrorView(
        state = aAppErrorState()
    )
}

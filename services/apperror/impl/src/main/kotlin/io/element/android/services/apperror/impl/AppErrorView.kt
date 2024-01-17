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
        onDismiss = onDismiss,
    )
}

@PreviewsDayNight
@Composable
internal fun AppErrorViewPreview() = ElementPreview {
    AppErrorView(
        state = aAppErrorState()
    )
}

/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.messages.impl.timeline.focus

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.window.DialogProperties
import io.element.android.features.messages.impl.timeline.FocusRequestState
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.room.errors.FocusEventException
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun FocusRequestStateView(
    focusRequestState: FocusRequestState,
    onClearFocusRequestState: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (focusRequestState) {
        is FocusRequestState.Failure -> {
            val errorMessage = when (focusRequestState.throwable) {
                is FocusEventException.EventNotFound,
                is FocusEventException.InvalidEventId -> stringResource(id = CommonStrings.error_message_not_found)
                is FocusEventException.Other -> stringResource(id = CommonStrings.error_unknown)
                else -> stringResource(id = CommonStrings.error_unknown)
            }
            ErrorDialog(
                content = errorMessage,
                onDismiss = onClearFocusRequestState,
                modifier = modifier,
            )
        }
        FocusRequestState.Fetching -> {
            ProgressDialog(
                modifier = modifier,
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
                onDismissRequest = onClearFocusRequestState,
            )
        }
        else -> Unit
    }
}

@PreviewsDayNight
@Composable
internal fun FocusRequestStateViewPreview(
    @PreviewParameter(FocusRequestStateProvider::class) state: FocusRequestState,
) = ElementPreview {
    FocusRequestStateView(
        focusRequestState = state,
        onClearFocusRequestState = {},
    )
}

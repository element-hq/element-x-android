/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
                onSubmit = onClearFocusRequestState,
                modifier = modifier,
            )
        }
        is FocusRequestState.Loading -> {
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

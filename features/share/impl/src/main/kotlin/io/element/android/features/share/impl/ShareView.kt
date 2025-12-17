/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.core.RoomId

@Composable
fun ShareView(
    state: ShareState,
    onShareSuccess: (List<RoomId>) -> Unit,
) {
    AsyncActionView(
        async = state.shareAction,
        onSuccess = {
            onShareSuccess(it)
        },
        onErrorDismiss = {
            state.eventSink(ShareEvents.ClearError)
        },
    )
}

@PreviewsDayNight
@Composable
internal fun ShareViewPreview(@PreviewParameter(ShareStateProvider::class) state: ShareState) = ElementPreview {
    ShareView(
        state = state,
        onShareSuccess = {}
    )
}

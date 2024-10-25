/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.async

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction

open class AsyncActionProvider : PreviewParameterProvider<AsyncAction<Unit>> {
    override val values: Sequence<AsyncAction<Unit>>
        get() = sequenceOf(
            AsyncAction.Uninitialized,
            AsyncAction.ConfirmingNoParams,
            AsyncAction.Loading,
            AsyncAction.Failure(Exception("An error occurred")),
            AsyncAction.Success(Unit),
        )
}

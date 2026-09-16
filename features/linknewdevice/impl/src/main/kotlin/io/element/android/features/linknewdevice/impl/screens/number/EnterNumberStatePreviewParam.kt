/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.linknewdevice.impl.screens.number

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.linknewdevice.ErrorType

open class EnterNumberStatePreviewParam : PreviewParameterProvider<EnterNumberState> {
    override val values: Sequence<EnterNumberState>
        get() = sequenceOf(
            aEnterNumberState(),
            aEnterNumberState(number = "1"),
            aEnterNumberState(number = "12"),
            aEnterNumberState(number = "12", sendingCode = AsyncAction.Loading),
            aEnterNumberState(number = "12", sendingCode = AsyncAction.Failure(ErrorType.InvalidCheckCode("Invalid"))),
            aEnterNumberState(number = "12", sendingCode = AsyncAction.Failure(Exception("Failed to send code"))),
        )
}

fun aEnterNumberState(
    number: String = "",
    sendingCode: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (EnterNumberEvent) -> Unit = {},
) = EnterNumberState(
    number = number,
    sendingCode = sendingCode,
    eventSink = eventSink,
)

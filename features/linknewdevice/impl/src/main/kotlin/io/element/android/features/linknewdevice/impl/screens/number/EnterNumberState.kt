/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.linknewdevice.impl.screens.number

import io.element.android.features.linknewdevice.impl.screens.number.model.Number
import io.element.android.libraries.architecture.AsyncAction

data class EnterNumberState(
    val number: String,
    val sendingCode: AsyncAction<Unit>,
    val eventSink: (EnterNumberEvent) -> Unit,
) {
    val numberEntry = Number.createEmpty(Config.VERIFICATION_CODE_LENGTH).fillWith(number)
    val isContinueButtonEnabled: Boolean
        get() = numberEntry.isComplete() && !sendingCode.isLoading()
}

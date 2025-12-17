/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.setup.biometric

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class SetupBiometricStateProvider : PreviewParameterProvider<SetupBiometricState> {
    override val values: Sequence<SetupBiometricState>
        get() = sequenceOf(
            aSetupBiometricState(),
        )
}

fun aSetupBiometricState(
    isBiometricSetupDone: Boolean = false,
) = SetupBiometricState(
    isBiometricSetupDone = isBiometricSetupDone,
    eventSink = {}
)

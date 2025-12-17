/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ftue.impl.sessionverification.choosemode

import io.element.android.features.logout.api.direct.DirectLogoutState
import io.element.android.libraries.architecture.AsyncData

data class ChooseSelfVerificationModeState(
    val buttonsState: AsyncData<ButtonsState>,
    val directLogoutState: DirectLogoutState,
    val eventSink: (ChooseSelfVerificationModeEvent) -> Unit,
) {
    data class ButtonsState(
        val canUseAnotherDevice: Boolean,
        val canEnterRecoveryKey: Boolean,
    )
}

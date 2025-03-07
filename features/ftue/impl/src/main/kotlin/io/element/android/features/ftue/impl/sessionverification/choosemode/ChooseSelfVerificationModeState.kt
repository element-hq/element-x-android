/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ftue.impl.sessionverification.choosemode

import io.element.android.features.logout.api.direct.DirectLogoutState

data class ChooseSelfVerificationModeState(
    val isLastDevice: Boolean,
    val canEnterRecoveryKey: Boolean,
    val directLogoutState: DirectLogoutState,
    val eventSink: (ChooseSelfVerificationModeEvent) -> Unit,
)

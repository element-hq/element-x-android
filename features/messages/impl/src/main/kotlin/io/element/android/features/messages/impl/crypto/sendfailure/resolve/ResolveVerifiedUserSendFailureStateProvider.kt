/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.sendfailure.resolve

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.messages.impl.crypto.sendfailure.VerifiedUserSendFailure
import io.element.android.libraries.architecture.AsyncAction

open class ResolveVerifiedUserSendFailureStateProvider : PreviewParameterProvider<ResolveVerifiedUserSendFailureState> {
    override val values: Sequence<ResolveVerifiedUserSendFailureState>
        get() = sequenceOf(
            aResolveVerifiedUserSendFailureState(),
            aResolveVerifiedUserSendFailureState(
                verifiedUserSendFailure = anUnsignedDeviceSendFailure()
            ),
            aResolveVerifiedUserSendFailureState(
                verifiedUserSendFailure = aChangedIdentitySendFailure()
            )
        )
}

fun aResolveVerifiedUserSendFailureState(
    verifiedUserSendFailure: VerifiedUserSendFailure = VerifiedUserSendFailure.None,
    resolveAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    retryAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (ResolveVerifiedUserSendFailureEvents) -> Unit = {}
) = ResolveVerifiedUserSendFailureState(
    verifiedUserSendFailure = verifiedUserSendFailure,
    resolveAction = resolveAction,
    retryAction = retryAction,
    eventSink = eventSink
)

fun anUnsignedDeviceSendFailure(userDisplayName: String = "Alice") = VerifiedUserSendFailure.UnsignedDevice.FromOther(
    userDisplayName = userDisplayName,
)

fun aChangedIdentitySendFailure(userDisplayName: String = "Alice") = VerifiedUserSendFailure.ChangedIdentity(
    userDisplayName = userDisplayName,
)

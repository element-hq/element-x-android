/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.sendfailure.resolve

import io.element.android.features.messages.impl.crypto.sendfailure.VerifiedUserSendFailure
import io.element.android.libraries.architecture.AsyncAction

data class ResolveVerifiedUserSendFailureState(
    val verifiedUserSendFailure: VerifiedUserSendFailure,
    val resolveAction: AsyncAction<Unit>,
    val retryAction: AsyncAction<Unit>,
    val eventSink: (ResolveVerifiedUserSendFailureEvents) -> Unit
)

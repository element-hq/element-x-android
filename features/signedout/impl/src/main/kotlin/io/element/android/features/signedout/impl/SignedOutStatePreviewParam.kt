/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.signedout.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUser

open class SignedOutStatePreviewParam : PreviewParameterProvider<SignedOutState> {
    override val values: Sequence<SignedOutState>
        get() = sequenceOf(
            aSignedOutState(),
            aSignedOutState(signedOutSession = aMatrixUser(id = "@alice:server.org", displayName = null)),
        )
}

internal fun aSignedOutState(
    signedOutSession: MatrixUser = aMatrixUser(id = "@alice:server.org", displayName = "Alice"),
    eventSink: (SignedOutEvent) -> Unit = {},
) = SignedOutState(
    signedOutMatrixUser = signedOutSession,
    eventSink = eventSink,
)

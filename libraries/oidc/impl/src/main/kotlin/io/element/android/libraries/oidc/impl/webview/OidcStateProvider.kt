/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.oidc.impl.webview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.auth.OidcDetails

open class OidcStateProvider : PreviewParameterProvider<OidcState> {
    override val values: Sequence<OidcState>
        get() = sequenceOf(
            aOidcState(),
            aOidcState().copy(requestState = AsyncAction.Loading),
        )
}

fun aOidcState() = OidcState(
    oidcDetails = aOidcDetails(),
    requestState = AsyncAction.Uninitialized,
    eventSink = {}
)

fun aOidcDetails() = OidcDetails(
    url = "aUrl",
)

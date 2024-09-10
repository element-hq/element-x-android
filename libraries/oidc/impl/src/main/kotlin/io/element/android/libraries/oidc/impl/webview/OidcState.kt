/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.oidc.impl.webview

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.auth.OidcDetails

data class OidcState(
    val oidcDetails: OidcDetails,
    val requestState: AsyncAction<Unit>,
    val eventSink: (OidcEvents) -> Unit
)

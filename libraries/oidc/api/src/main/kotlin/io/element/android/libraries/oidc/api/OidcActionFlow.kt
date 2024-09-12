/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.oidc.api

import kotlinx.coroutines.flow.FlowCollector

interface OidcActionFlow {
    fun post(oidcAction: OidcAction)
    suspend fun collect(collector: FlowCollector<OidcAction?>)
    fun reset()
}

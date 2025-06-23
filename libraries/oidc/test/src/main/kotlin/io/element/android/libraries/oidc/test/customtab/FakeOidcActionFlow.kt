/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.oidc.test.customtab

import io.element.android.libraries.oidc.api.OidcAction
import io.element.android.libraries.oidc.api.OidcActionFlow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * This is actually a copy of DefaultOidcActionFlow.
 */
class FakeOidcActionFlow : OidcActionFlow {
    private val mutableStateFlow = MutableStateFlow<OidcAction?>(null)

    override fun post(oidcAction: OidcAction) {
        mutableStateFlow.value = oidcAction
    }

    override suspend fun collect(collector: FlowCollector<OidcAction?>) {
        mutableStateFlow.collect(collector)
    }

    override fun reset() {
        mutableStateFlow.value = null
    }
}

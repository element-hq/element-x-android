/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.oidc.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.oidc.api.OidcAction
import io.element.android.libraries.oidc.api.OidcActionFlow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultOidcActionFlow @Inject constructor() : OidcActionFlow {
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

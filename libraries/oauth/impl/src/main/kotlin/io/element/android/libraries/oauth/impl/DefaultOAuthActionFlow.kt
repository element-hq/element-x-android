/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.oauth.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.oauth.api.OAuthAction
import io.element.android.libraries.oauth.api.OAuthActionFlow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultOAuthActionFlow : OAuthActionFlow {
    private val mutableStateFlow = MutableStateFlow<OAuthAction?>(null)

    override fun post(oAuthAction: OAuthAction) {
        mutableStateFlow.value = oAuthAction
    }

    override suspend fun collect(collector: FlowCollector<OAuthAction?>) {
        mutableStateFlow.collect(collector)
    }

    override fun reset() {
        mutableStateFlow.value = null
    }
}

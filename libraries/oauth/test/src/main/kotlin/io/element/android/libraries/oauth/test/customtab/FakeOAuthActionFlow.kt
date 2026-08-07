/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.oauth.test.customtab

import io.element.android.libraries.oauth.api.OAuthAction
import io.element.android.libraries.oauth.api.OAuthActionFlow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * This is actually a copy of DefaultOAuthActionFlow.
 */
class FakeOAuthActionFlow : OAuthActionFlow {
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

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.network.RetrofitFactory
import io.element.android.libraries.pushproviders.unifiedpush.network.UnifiedPushApi

interface UnifiedPushApiFactory {
    fun create(baseUrl: String): UnifiedPushApi
}

@ContributesBinding(AppScope::class)
class DefaultUnifiedPushApiFactory(
    private val retrofitFactory: RetrofitFactory,
) : UnifiedPushApiFactory {
    override fun create(baseUrl: String): UnifiedPushApi {
        return retrofitFactory.create(baseUrl)
            .create(UnifiedPushApi::class.java)
    }
}

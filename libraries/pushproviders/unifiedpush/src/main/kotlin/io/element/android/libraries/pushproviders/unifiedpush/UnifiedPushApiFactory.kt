/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.network.RetrofitFactory
import io.element.android.libraries.pushproviders.unifiedpush.network.UnifiedPushApi
import javax.inject.Inject

interface UnifiedPushApiFactory {
    fun create(baseUrl: String): UnifiedPushApi
}

@ContributesBinding(AppScope::class)
class DefaultUnifiedPushApiFactory @Inject constructor(
    private val retrofitFactory: RetrofitFactory,
) : UnifiedPushApiFactory {
    override fun create(baseUrl: String): UnifiedPushApi {
        return retrofitFactory.create(baseUrl)
            .create(UnifiedPushApi::class.java)
    }
}

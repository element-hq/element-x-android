/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.pushgateway

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.network.RetrofitFactory
import javax.inject.Inject

interface PushGatewayApiFactory {
    fun create(baseUrl: String): PushGatewayAPI
}

@ContributesBinding(AppScope::class)
class DefaultPushGatewayApiFactory @Inject constructor(
    private val retrofitFactory: RetrofitFactory,
) : PushGatewayApiFactory {
    override fun create(baseUrl: String): PushGatewayAPI {
        return retrofitFactory.create(baseUrl)
            .create(PushGatewayAPI::class.java)
    }
}

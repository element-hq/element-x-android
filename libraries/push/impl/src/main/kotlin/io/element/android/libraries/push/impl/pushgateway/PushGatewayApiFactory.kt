/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.pushgateway

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.libraries.network.RetrofitFactory

interface PushGatewayApiFactory {
    fun create(baseUrl: String): PushGatewayAPI
}

@ContributesBinding(AppScope::class)
@Inject
class DefaultPushGatewayApiFactory(
    private val retrofitFactory: RetrofitFactory,
) : PushGatewayApiFactory {
    override fun create(baseUrl: String): PushGatewayAPI {
        return retrofitFactory.create(baseUrl)
            .create(PushGatewayAPI::class.java)
    }
}

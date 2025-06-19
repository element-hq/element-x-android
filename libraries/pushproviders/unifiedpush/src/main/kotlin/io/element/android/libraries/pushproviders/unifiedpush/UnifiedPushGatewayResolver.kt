/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.di.AppScope
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

sealed interface UnifiedPushGatewayResolverResult {
    data class Success(val gateway: String) : UnifiedPushGatewayResolverResult
    data class Error(val gateway: String) : UnifiedPushGatewayResolverResult
    data object NoMatrixGateway : UnifiedPushGatewayResolverResult
    data object ErrorInvalidUrl : UnifiedPushGatewayResolverResult
}

interface UnifiedPushGatewayResolver {
    suspend fun getGateway(endpoint: String): UnifiedPushGatewayResolverResult
}

private val loggerTag = LoggerTag("DefaultUnifiedPushGatewayResolver")

@ContributesBinding(AppScope::class)
class DefaultUnifiedPushGatewayResolver @Inject constructor(
    private val unifiedPushApiFactory: UnifiedPushApiFactory,
    private val coroutineDispatchers: CoroutineDispatchers,
) : UnifiedPushGatewayResolver {
    override suspend fun getGateway(endpoint: String): UnifiedPushGatewayResolverResult {
        val url = tryOrNull(
            onException = { Timber.tag(loggerTag.value).d(it, "Cannot parse endpoint as an URL") }
        ) {
            URL(endpoint)
        }
        return if (url == null) {
            Timber.tag(loggerTag.value).d("ErrorInvalidUrl")
            UnifiedPushGatewayResolverResult.ErrorInvalidUrl
        } else {
            val port = if (url.port != -1) ":${url.port}" else ""
            val customBase = "${url.protocol}://${url.host}$port"
            val customUrl = "$customBase/_matrix/push/v1/notify"
            Timber.tag(loggerTag.value).i("Testing $customUrl")
            return withContext(coroutineDispatchers.io) {
                val api = unifiedPushApiFactory.create(customBase)
                try {
                    val discoveryResponse = api.discover()
                    if (discoveryResponse.unifiedpush.gateway == "matrix") {
                        Timber.tag(loggerTag.value).d("The endpoint seems to be a valid UnifiedPush gateway")
                        UnifiedPushGatewayResolverResult.Success(customUrl)
                    } else {
                        // The endpoint returned a 200 OK but didn't promote an actual matrix gateway, which means it doesn't have any
                        Timber.tag(loggerTag.value).w("The endpoint does not seem to be a valid UnifiedPush gateway, using fallback")
                        UnifiedPushGatewayResolverResult.NoMatrixGateway
                    }
                } catch (throwable: Throwable) {
                    if ((throwable as? HttpException)?.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                        Timber.tag(loggerTag.value).i("Checking for UnifiedPush endpoint yielded 404, using fallback")
                        UnifiedPushGatewayResolverResult.NoMatrixGateway
                    } else {
                        Timber.tag(loggerTag.value).e(throwable, "Error checking for UnifiedPush endpoint")
                        UnifiedPushGatewayResolverResult.Error(customUrl)
                    }
                }
            }
        }
    }
}

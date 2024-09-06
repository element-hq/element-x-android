/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import android.content.Context
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.pushproviders.api.PusherSubscriber
import org.unifiedpush.android.connector.UnifiedPush
import timber.log.Timber
import javax.inject.Inject

interface UnregisterUnifiedPushUseCase {
    suspend fun execute(matrixClient: MatrixClient, clientSecret: String): Result<Unit>
}

@ContributesBinding(AppScope::class)
class DefaultUnregisterUnifiedPushUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val unifiedPushStore: UnifiedPushStore,
    private val pusherSubscriber: PusherSubscriber,
) : UnregisterUnifiedPushUseCase {
    override suspend fun execute(matrixClient: MatrixClient, clientSecret: String): Result<Unit> {
        val endpoint = unifiedPushStore.getEndpoint(clientSecret)
        val gateway = unifiedPushStore.getPushGateway(clientSecret)
        if (endpoint == null || gateway == null) {
            Timber.w("No endpoint or gateway found for client secret")
            // Ensure we don't have any remaining data, but ignore this error
            unifiedPushStore.storeUpEndpoint(clientSecret, null)
            unifiedPushStore.storePushGateway(clientSecret, null)
            return Result.success(Unit)
        }
        return pusherSubscriber.unregisterPusher(matrixClient, endpoint, gateway)
            .onSuccess {
                unifiedPushStore.storeUpEndpoint(clientSecret, null)
                unifiedPushStore.storePushGateway(clientSecret, null)
                UnifiedPush.unregisterApp(context)
            }
    }
}

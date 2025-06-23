/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import android.content.Context
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.unifiedpush.registration.EndpointRegistrationHandler
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import org.unifiedpush.android.connector.UnifiedPush
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

interface RegisterUnifiedPushUseCase {
    suspend fun execute(distributor: Distributor, clientSecret: String): Result<Unit>
}

@ContributesBinding(AppScope::class)
class DefaultRegisterUnifiedPushUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val endpointRegistrationHandler: EndpointRegistrationHandler,
) : RegisterUnifiedPushUseCase {
    override suspend fun execute(distributor: Distributor, clientSecret: String): Result<Unit> {
        UnifiedPush.saveDistributor(context, distributor.value)
        // This will trigger the callback
        // VectorUnifiedPushMessagingReceiver.onNewEndpoint
        UnifiedPush.register(context = context, instance = clientSecret)
        // Wait for VectorUnifiedPushMessagingReceiver.onNewEndpoint to proceed
        @Suppress("RunCatchingNotAllowed")
        return runCatching {
            withTimeout(30.seconds) {
                val result = endpointRegistrationHandler.state
                    .filter { it.clientSecret == clientSecret }
                    .first()
                    .result
                result.getOrThrow()
            }
        }
    }
}

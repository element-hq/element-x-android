/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.feral.connection

import dev.zacsweers.metro.Inject
import io.element.android.appconfig.FeralPushConfig
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.push.api.push.FetchPushForegroundServiceManager
import io.element.android.libraries.pushproviders.api.PushHandler
import io.element.android.libraries.pushproviders.feral.FeralPushRegistration
import io.element.android.libraries.pushproviders.feral.FeralPushStore
import io.element.android.libraries.pushproviders.unifiedpush.UnifiedPushParser
import timber.log.Timber

private val loggerTag = LoggerTag("FeralPushMessageHandler", LoggerTag.PushLoggerTag)

/**
 * Turns a ntfy `message` frame into a [PushHandler] call: the body is the Matrix push-gateway
 * notification JSON, parsed by the UnifiedPush parser with the session's client secret.
 */
@Inject
class FeralPushMessageHandler(
    private val parser: UnifiedPushParser,
    private val pushHandler: PushHandler,
    private val feralPushStore: FeralPushStore,
    private val fetchPushForegroundServiceManager: FetchPushForegroundServiceManager,
) {
    suspend fun handle(registration: FeralPushRegistration, frame: FeralPushFrame) {
        // Keep the device awake while the push is handled and the fetch work scheduled (same as UnifiedPush).
        fetchPushForegroundServiceManager.start()
        val body = frame.body()
        val pushData = parser.parse(body, registration.clientSecret)
        if (pushData == null) {
            Timber.tag(loggerTag.value).w("Invalid data received from the Feral server")
            pushHandler.handleInvalid(providerInfo = FeralPushConfig.NAME, data = String(body))
            fetchPushForegroundServiceManager.stop()
        } else {
            val handled = pushHandler.handle(pushData = pushData, providerInfo = FeralPushConfig.NAME)
            if (!handled) {
                fetchPushForegroundServiceManager.stop()
            }
        }
        if (frame.id.isNotEmpty()) {
            feralPushStore.setLastMessageId(registration.session, frame.id)
        }
    }
}

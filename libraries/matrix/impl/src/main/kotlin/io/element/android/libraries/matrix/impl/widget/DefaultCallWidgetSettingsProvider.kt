/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.widget

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.matrix.api.widget.CallAnalyticCredentialsProvider
import io.element.android.libraries.matrix.api.widget.CallWidgetSettingsProvider
import io.element.android.libraries.matrix.api.widget.MatrixWidgetSettings
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.flow.first
import org.matrix.rustcomponents.sdk.newVirtualElementCallWidget
import timber.log.Timber
import uniffi.matrix_sdk.EncryptionSystem
import uniffi.matrix_sdk.VirtualElementCallWidgetConfig
import uniffi.matrix_sdk.VirtualElementCallWidgetProperties
import uniffi.matrix_sdk.Intent as CallIntent

@ContributesBinding(AppScope::class)
class DefaultCallWidgetSettingsProvider(
    private val buildMeta: BuildMeta,
    private val callAnalyticsCredentialsProvider: CallAnalyticCredentialsProvider,
    private val analyticsService: AnalyticsService,
) : CallWidgetSettingsProvider {
    override suspend fun provide(baseUrl: String, widgetId: String, encrypted: Boolean, direct: Boolean, hasActiveCall: Boolean): MatrixWidgetSettings {
        val isAnalyticsEnabled = analyticsService.userConsentFlow.first()
        val properties = VirtualElementCallWidgetProperties(
            elementCallUrl = baseUrl,
            widgetId = widgetId,
            fontScale = null,
            font = null,
            encryption = if (encrypted) EncryptionSystem.PerParticipantKeys else EncryptionSystem.Unencrypted,
            posthogUserId = callAnalyticsCredentialsProvider.posthogUserId.takeIf { isAnalyticsEnabled },
            posthogApiHost = callAnalyticsCredentialsProvider.posthogApiHost.takeIf { isAnalyticsEnabled },
            posthogApiKey = callAnalyticsCredentialsProvider.posthogApiKey.takeIf { isAnalyticsEnabled },
            rageshakeSubmitUrl = callAnalyticsCredentialsProvider.rageshakeSubmitUrl,
            sentryDsn = callAnalyticsCredentialsProvider.sentryDsn.takeIf { isAnalyticsEnabled },
            sentryEnvironment = if (buildMeta.buildType == BuildType.RELEASE) "RELEASE" else "DEBUG",
            parentUrl = null,
        )
        val config = VirtualElementCallWidgetConfig(
            // TODO remove this once we have the next EC version
            preload = false,
            // TODO remove this once we have the next EC version
            skipLobby = null,
            intent = when {
                direct && hasActiveCall -> CallIntent.JOIN_EXISTING_DM
                hasActiveCall -> CallIntent.JOIN_EXISTING
                direct -> CallIntent.START_CALL_DM
                else -> CallIntent.START_CALL
            }.also {
                Timber.d("Starting/joining call with intent: $it")
            }
        )
        val rustWidgetSettings = newVirtualElementCallWidget(
            props = properties,
            config = config,
        )
        return MatrixWidgetSettings.fromRustWidgetSettings(rustWidgetSettings)
    }
}

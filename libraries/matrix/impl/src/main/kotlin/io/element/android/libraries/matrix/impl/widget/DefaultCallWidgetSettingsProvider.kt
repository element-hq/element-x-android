/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.widget

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.matrix.api.widget.CallAnalyticCredentialsProvider
import io.element.android.libraries.matrix.api.widget.CallWidgetSettingsProvider
import io.element.android.libraries.matrix.api.widget.MatrixWidgetSettings
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.flow.first
import org.matrix.rustcomponents.sdk.newVirtualElementCallWidget
import uniffi.matrix_sdk.EncryptionSystem
import uniffi.matrix_sdk.HeaderStyle
import uniffi.matrix_sdk.NotificationType
import uniffi.matrix_sdk.VirtualElementCallWidgetConfig
import uniffi.matrix_sdk.VirtualElementCallWidgetProperties
import uniffi.matrix_sdk.Intent as CallIntent

@ContributesBinding(AppScope::class)
@Inject
class DefaultCallWidgetSettingsProvider(
    private val buildMeta: BuildMeta,
    private val callAnalyticsCredentialsProvider: CallAnalyticCredentialsProvider,
    private val analyticsService: AnalyticsService,
) : CallWidgetSettingsProvider {
    override suspend fun provide(baseUrl: String, widgetId: String, encrypted: Boolean, direct: Boolean): MatrixWidgetSettings {
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
            preload = null,
            appPrompt = false,
            confineToRoom = true,
            // TODO We probably want to provide different values for this field.
            intent = CallIntent.START_CALL,
            hideScreensharing = false,
            // For backwards compatibility, it'll be ignored in recent versions of Element Call
            hideHeader = true,
            controlledAudioDevices = true,
            header = HeaderStyle.APP_BAR,
            sendNotificationType = if (direct) NotificationType.RING else NotificationType.NOTIFICATION,
            skipLobby = null,
        )
        val rustWidgetSettings = newVirtualElementCallWidget(
            props = properties,
            config = config,
        )
        return MatrixWidgetSettings.fromRustWidgetSettings(rustWidgetSettings)
    }
}

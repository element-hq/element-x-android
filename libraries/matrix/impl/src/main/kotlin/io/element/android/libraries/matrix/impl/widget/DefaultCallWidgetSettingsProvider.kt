/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.widget

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.widget.CallAnalyticCredentialsProvider
import io.element.android.libraries.matrix.api.widget.CallWidgetSettingsProvider
import io.element.android.libraries.matrix.api.widget.MatrixWidgetSettings
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.flow.first
import org.matrix.rustcomponents.sdk.EncryptionSystem
import org.matrix.rustcomponents.sdk.VirtualElementCallWidgetOptions
import org.matrix.rustcomponents.sdk.newVirtualElementCallWidget
import javax.inject.Inject
import org.matrix.rustcomponents.sdk.Intent as CallIntent

@ContributesBinding(AppScope::class)
class DefaultCallWidgetSettingsProvider @Inject constructor(
    private val buildMeta: BuildMeta,
    private val callAnalyticsCredentialsProvider: CallAnalyticCredentialsProvider,
    private val analyticsService: AnalyticsService,
) : CallWidgetSettingsProvider {
    override suspend fun provide(baseUrl: String, widgetId: String, encrypted: Boolean): MatrixWidgetSettings {
        val isAnalyticsEnabled = analyticsService.userConsentFlow.first()
        val options = VirtualElementCallWidgetOptions(
            elementCallUrl = baseUrl,
            widgetId = widgetId,
            preload = null,
            fontScale = null,
            appPrompt = false,
            confineToRoom = true,
            font = null,
            encryption = if (encrypted) EncryptionSystem.PerParticipantKeys else EncryptionSystem.Unencrypted,
            intent = CallIntent.START_CALL,
            hideScreensharing = false,
            posthogUserId = callAnalyticsCredentialsProvider.posthogUserId.takeIf { isAnalyticsEnabled },
            posthogApiHost = callAnalyticsCredentialsProvider.posthogApiHost.takeIf { isAnalyticsEnabled },
            posthogApiKey = callAnalyticsCredentialsProvider.posthogApiKey.takeIf { isAnalyticsEnabled },
            rageshakeSubmitUrl = callAnalyticsCredentialsProvider.rageshakeSubmitUrl,
            sentryDsn = callAnalyticsCredentialsProvider.sentryDsn.takeIf { isAnalyticsEnabled },
            sentryEnvironment = if (buildMeta.buildType == BuildType.RELEASE) "RELEASE" else "DEBUG",
            parentUrl = null,
            hideHeader = true,
            controlledMediaDevices = true,
        )
        val rustWidgetSettings = newVirtualElementCallWidget(options)
        return MatrixWidgetSettings.fromRustWidgetSettings(rustWidgetSettings)
    }
}

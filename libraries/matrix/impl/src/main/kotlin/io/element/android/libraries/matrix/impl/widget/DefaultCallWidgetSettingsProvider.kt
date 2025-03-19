/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.widget

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.appconfig.RageshakeConfig
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.widget.CallWidgetSettingsProvider
import io.element.android.libraries.matrix.api.widget.MatrixWidgetSettings
import io.element.android.services.analytics.api.AnalyticsStore
import io.element.android.services.analyticsproviders.posthog.PosthogEndpointConfigProvider
import io.element.android.services.analyticsproviders.sentry.SentryAnalyticsProvider
import io.element.android.services.analyticsproviders.sentry.SentryConfig
import kotlinx.coroutines.flow.first
import org.matrix.rustcomponents.sdk.EncryptionSystem
import org.matrix.rustcomponents.sdk.Intent
import org.matrix.rustcomponents.sdk.VirtualElementCallWidgetOptions
import org.matrix.rustcomponents.sdk.newVirtualElementCallWidget
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultCallWidgetSettingsProvider @Inject constructor(
    private val buildMeta: BuildMeta,
    private val posthogEndpointConfigProvider: PosthogEndpointConfigProvider,
    private val analyticsStore: AnalyticsStore,
) : CallWidgetSettingsProvider {
    override suspend fun provide(baseUrl: String, widgetId: String, encrypted: Boolean): MatrixWidgetSettings {
        val analyticsEnabled = analyticsStore.userConsentFlow.first()
        val posthogEndpointConfig = posthogEndpointConfigProvider.provide()
        val options = VirtualElementCallWidgetOptions(
            elementCallUrl = baseUrl,
            widgetId = widgetId,
            parentUrl = null,
            hideHeader = null,
            preload = null,
            fontScale = null,
            appPrompt = false,
            confineToRoom = true,
            font = null,
            encryption = if (encrypted) EncryptionSystem.PerParticipantKeys else EncryptionSystem.Unencrypted,
            intent = Intent.START_CALL,
            hideScreensharing = false,
            posthogUserId = null,
            posthogApiHost = posthogEndpointConfig.host.takeIf { analyticsEnabled },
            posthogApiKey = posthogEndpointConfig.apiKey.takeIf { analyticsEnabled },
            rageshakeSubmitUrl = RageshakeConfig.BUG_REPORT_URL,
            sentryDsn = SentryConfig.DSN.takeIf { analyticsEnabled },
            sentryEnvironment = if (buildMeta.buildType == BuildType.RELEASE) SentryConfig.ENV_RELEASE else SentryConfig.ENV_DEBUG,
        )
        val rustWidgetSettings = newVirtualElementCallWidget(options)
        return MatrixWidgetSettings.fromRustWidgetSettings(rustWidgetSettings)
    }
}

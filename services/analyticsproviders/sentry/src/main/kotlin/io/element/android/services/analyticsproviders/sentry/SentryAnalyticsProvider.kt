/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analyticsproviders.sentry

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import im.vector.app.features.analytics.itf.VectorAnalyticsEvent
import im.vector.app.features.analytics.itf.VectorAnalyticsScreen
import im.vector.app.features.analytics.plan.SuperProperties
import im.vector.app.features.analytics.plan.UserProperties
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.services.analyticsproviders.api.AnalyticsProvider
import io.element.android.services.analyticsproviders.api.AnalyticsTransaction
import io.element.android.services.analyticsproviders.sentry.log.analyticsTag
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryOptions
import io.sentry.android.core.SentryAndroid
import timber.log.Timber

@ContributesIntoSet(AppScope::class)
@Inject
class SentryAnalyticsProvider(
    @ApplicationContext private val context: Context,
    private val buildMeta: BuildMeta,
) : AnalyticsProvider {
    override val name = SentryConfig.NAME

    override fun init() {
        Timber.tag(analyticsTag.value).d("Initializing Sentry")
        if (Sentry.isEnabled()) return

        val dsn = SentryConfig.DSN.ifBlank {
            Timber.w("No Sentry DSN provided, Sentry will not be initialized")
            return
        }

        SentryAndroid.init(context) { options ->
            options.dsn = dsn
            options.beforeSend = SentryOptions.BeforeSendCallback { event, _ -> event }
            options.tracesSampleRate = 1.0
            options.isEnableUserInteractionTracing = true
            options.environment = buildMeta.buildType.toSentryEnv()
        }
        Timber.tag(analyticsTag.value).d("Sentry was initialized correctly")
    }

    override fun stop() {
        Timber.tag(analyticsTag.value).d("Stopping Sentry")
        Sentry.close()
    }

    override fun capture(event: VectorAnalyticsEvent) {
        val breadcrumb = Breadcrumb(event.getName()).apply {
            category = "event"
            for ((key, value) in event.getProperties().orEmpty()) {
                setData(key, value.toString())
            }
        }
        Sentry.addBreadcrumb(breadcrumb)
    }

    override fun screen(screen: VectorAnalyticsScreen) {
        val breadcrumb = Breadcrumb(screen.getName()).apply {
            category = "screen"
            for ((key, value) in screen.getProperties().orEmpty()) {
                setData(key, value.toString())
            }
        }
        Sentry.addBreadcrumb(breadcrumb)
    }

    override fun updateUserProperties(userProperties: UserProperties) {
    }

    override fun updateSuperProperties(updatedProperties: SuperProperties) {
    }

    override fun trackError(throwable: Throwable) {
        Sentry.captureException(throwable)
    }

    override fun startTransaction(name: String, operation: String?): AnalyticsTransaction? {
        return SentryAnalyticsTransaction(name, operation)
    }
}

private fun BuildType.toSentryEnv() = when (this) {
    BuildType.RELEASE -> SentryConfig.ENV_RELEASE
    BuildType.NIGHTLY -> SentryConfig.ENV_NIGHTLY
    BuildType.DEBUG -> SentryConfig.ENV_DEBUG
}

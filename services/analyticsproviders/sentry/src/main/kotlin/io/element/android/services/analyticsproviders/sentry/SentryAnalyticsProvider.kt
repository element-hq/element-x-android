/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.services.analyticsproviders.sentry

import android.content.Context
import com.squareup.anvil.annotations.ContributesMultibinding
import im.vector.app.features.analytics.itf.VectorAnalyticsEvent
import im.vector.app.features.analytics.itf.VectorAnalyticsScreen
import im.vector.app.features.analytics.plan.SuperProperties
import im.vector.app.features.analytics.plan.UserProperties
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.services.analyticsproviders.api.AnalyticsProvider
import io.element.android.services.analyticsproviders.sentry.log.analyticsTag
import io.sentry.Sentry
import io.sentry.SentryOptions
import io.sentry.android.core.SentryAndroid
import timber.log.Timber
import javax.inject.Inject

@ContributesMultibinding(AppScope::class)
class SentryAnalyticsProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val buildMeta: BuildMeta,
) : AnalyticsProvider {
    override val name = SentryConfig.NAME

    override fun init() {
        Timber.tag(analyticsTag.value).d("Initializing Sentry")
        if (Sentry.isEnabled()) return
        SentryAndroid.init(context) { options ->
            options.dsn = SentryConfig.DNS
            options.beforeSend = SentryOptions.BeforeSendCallback { event, _ -> event }
            options.tracesSampleRate = 1.0
            options.isEnableUserInteractionTracing = true
            options.environment = buildMeta.buildType.toSentryEnv()
            options.diagnosticLevel
        }
    }

    override fun stop() {
        Timber.tag(analyticsTag.value).d("Stopping Sentry")
        Sentry.close()
    }

    override fun capture(event: VectorAnalyticsEvent) {
    }

    override fun screen(screen: VectorAnalyticsScreen) {
    }

    override fun updateUserProperties(userProperties: UserProperties) {
    }

    override fun updateSuperProperties(updatedProperties: SuperProperties) {
    }

    override fun trackError(throwable: Throwable) {
        Sentry.captureException(throwable)
    }
}

private fun BuildType.toSentryEnv() = when (this) {
    BuildType.RELEASE -> SentryConfig.ENV_RELEASE
    BuildType.NIGHTLY,
    BuildType.DEBUG -> SentryConfig.ENV_DEBUG
}

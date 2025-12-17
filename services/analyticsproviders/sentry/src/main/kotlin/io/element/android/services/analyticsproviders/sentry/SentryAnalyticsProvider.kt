/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analyticsproviders.sentry

import android.content.Context
import androidx.annotation.VisibleForTesting
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import im.vector.app.features.analytics.itf.VectorAnalyticsEvent
import im.vector.app.features.analytics.itf.VectorAnalyticsScreen
import im.vector.app.features.analytics.plan.SuperProperties
import im.vector.app.features.analytics.plan.UserProperties
import io.element.android.libraries.core.data.ByteUnit
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.di.identifiers.SentryDsn
import io.element.android.libraries.matrix.api.analytics.GetDatabaseSizesUseCase
import io.element.android.services.analyticsproviders.api.AnalyticsProvider
import io.element.android.services.analyticsproviders.api.AnalyticsTransaction
import io.element.android.services.analyticsproviders.api.AnalyticsUserData
import io.element.android.services.analyticsproviders.sentry.log.analyticsTag
import io.element.android.services.appnavstate.api.AppNavigationStateService
import io.element.android.services.appnavstate.api.currentSessionId
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryOptions
import io.sentry.android.core.SentryAndroid
import io.sentry.protocol.SentryTransaction
import kotlinx.coroutines.runBlocking
import timber.log.Timber

@ContributesIntoSet(AppScope::class)
@Inject
class SentryAnalyticsProvider(
    @ApplicationContext private val context: Context,
    private val sentryDsn: SentryDsn?,
    private val buildMeta: BuildMeta,
    private val getDatabaseSizesUseCase: GetDatabaseSizesUseCase,
    private val appNavigationStateService: AppNavigationStateService,
) : AnalyticsProvider {
    override val name = SentryConfig.NAME

    override fun init() {
        Timber.tag(analyticsTag.value).d("Initializing Sentry")
        if (Sentry.isEnabled()) return

        val dsn = sentryDsn?.value ?: run {
            Timber.w("No Sentry DSN provided, Sentry will not be initialized")
            return
        }

        SentryAndroid.init(context) { options ->
            options.dsn = dsn
            options.beforeSendTransaction = SentryOptions.BeforeSendTransactionCallback { transaction, _ ->
                prepareTransactionBeforeSend(transaction)
            }
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

    override fun addExtraData(key: String, value: String) {
        Sentry.setExtra(key, value)
    }

    override fun addIndexableData(key: String, value: String) {
        Sentry.setTag(key, value)
    }

    override fun trackError(throwable: Throwable) {
        Sentry.captureException(throwable)
    }

    override fun startTransaction(name: String, operation: String?): AnalyticsTransaction? {
        return SentryAnalyticsTransaction(name, operation)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun prepareTransactionBeforeSend(transaction: SentryTransaction): SentryTransaction {
        // Ensure we'll never upload any session ids in extras or tags
        val invalidExtras = transaction.extras?.filter { (it.value as? String)?.startsWith("@") == true }.orEmpty()
        for (invalidExtra in invalidExtras) {
            transaction.removeExtra(invalidExtra.key)
        }
        val invalidTags = transaction.tags?.filter { it.value.startsWith("@") }.orEmpty()
        for (invalidTag in invalidExtras) {
            transaction.removeTag(invalidTag.key)
        }

        val sessionId = appNavigationStateService.appNavigationState.value.navigationState.currentSessionId()
        if (sessionId != null) {
            // This runs in a separate thread, so although using `runBlocking` is not great, at least it shouldn't freeze the app
            // Also, the method is fairly quick, so the blocking shouldn't take longer than a few ms
            val databaseSizes = runBlocking { getDatabaseSizesUseCase(sessionId) }.getOrNull()

            databaseSizes?.stateStore?.let { transaction.setExtra(AnalyticsUserData.STATE_STORE_SIZE, it.into(ByteUnit.MB)) }
            databaseSizes?.eventCacheStore?.let { transaction.setExtra(AnalyticsUserData.EVENT_CACHE_SIZE, it.into(ByteUnit.MB)) }
            databaseSizes?.mediaStore?.let { transaction.setExtra(AnalyticsUserData.MEDIA_STORE_SIZE, it.into(ByteUnit.MB)) }
            databaseSizes?.cryptoStore?.let { transaction.setExtra(AnalyticsUserData.CRYPTO_STORE_SIZE, it.into(ByteUnit.MB)) }
        }
        return transaction
    }
}

private fun BuildType.toSentryEnv() = when (this) {
    BuildType.RELEASE -> SentryConfig.ENV_RELEASE
    BuildType.NIGHTLY -> SentryConfig.ENV_NIGHTLY
    BuildType.DEBUG -> SentryConfig.ENV_DEBUG
}

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analyticsproviders.posthog

import com.posthog.PostHogInterface
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import im.vector.app.features.analytics.itf.VectorAnalyticsEvent
import im.vector.app.features.analytics.itf.VectorAnalyticsScreen
import im.vector.app.features.analytics.plan.SuperProperties
import im.vector.app.features.analytics.plan.UserProperties
import io.element.android.services.analyticsproviders.api.AnalyticsProvider
import io.element.android.services.analyticsproviders.api.AnalyticsTransaction
import io.element.android.services.analyticsproviders.posthog.log.analyticsTag
import timber.log.Timber

// private val REUSE_EXISTING_ID: String? = null
// private val IGNORED_OPTIONS: Options? = null

@ContributesIntoSet(AppScope::class)
@Inject
class PosthogAnalyticsProvider(
    private val postHogFactory: PostHogFactory,
) : AnalyticsProvider {
    override val name = "Posthog"

    private var posthog: PostHogInterface? = null
    private var analyticsId: String? = null

    private var pendingUserProperties: MutableMap<String, Any>? = null

    private var superProperties: SuperProperties? = null

    private val userPropertiesLock = Any()

    override fun init() {
        posthog = postHogFactory.createPosthog()
        posthog?.optIn()
        // Timber.e("PostHog distinctId: ${posthog?.distinctId()}")
        identifyPostHog()
    }

    override fun stop() {
        // When opting out, ensure that the queue is flushed first, or it will be flushed later (after user has revoked consent)
        posthog?.flush()
        posthog?.optOut()
        posthog?.close()
        posthog = null
        analyticsId = null
    }

    override fun capture(event: VectorAnalyticsEvent) {
        synchronized(userPropertiesLock) {
            posthog?.capture(
                event = event.getName(),
                properties = event.getProperties()?.keepOnlyNonNullValues().withSuperProperties(),
                userProperties = pendingUserProperties,
            )
            pendingUserProperties = null
        }
    }

    override fun screen(screen: VectorAnalyticsScreen) {
        posthog?.screen(
            screenTitle = screen.getName(),
            properties = screen.getProperties().withSuperProperties(),
        )
    }

    override fun updateUserProperties(userProperties: UserProperties) {
        synchronized(userPropertiesLock) {
            // The pending properties will be sent with the following capture call
            if (pendingUserProperties == null) {
                pendingUserProperties = HashMap()
            }
            userProperties.getProperties()?.let {
                pendingUserProperties?.putAll(it)
            }
            // We are not currently using `identify` in EAX, if it was the case
            // we could have called identify to update the user properties.
            // For now, we have to store them, and they will be updated when the next call
            // to capture will happen.
        }
    }

    override fun updateSuperProperties(updatedProperties: SuperProperties) {
        this.superProperties = SuperProperties(
            cryptoSDK = updatedProperties.cryptoSDK ?: this.superProperties?.cryptoSDK,
            appPlatform = updatedProperties.appPlatform ?: this.superProperties?.appPlatform,
            cryptoSDKVersion = updatedProperties.cryptoSDKVersion ?: superProperties?.cryptoSDKVersion
        )
    }

    override fun trackError(throwable: Throwable) {
        // Not implemented
    }

    private fun identifyPostHog() {
        val id = analyticsId ?: return
        if (id.isEmpty()) {
            Timber.tag(analyticsTag.value).d("reset")
            posthog?.reset()
        } else {
            Timber.tag(analyticsTag.value).d("identify")
//            posthog?.identify(id, lateInitUserPropertiesFactory.createUserProperties()?.getProperties()?.toPostHogUserProperties(), IGNORED_OPTIONS)
        }
    }

    private fun Map<String, Any>?.withSuperProperties(): Map<String, Any>? {
        val withSuperProperties = this.orEmpty().toMutableMap()
        val superProperties = this@PosthogAnalyticsProvider.superProperties?.getProperties()
        superProperties?.forEach {
            if (!withSuperProperties.containsKey(it.key)) {
                withSuperProperties[it.key] = it.value
            }
        }
        return withSuperProperties.takeIf { it.isEmpty().not() }
    }

    override fun startTransaction(name: String, operation: String?): AnalyticsTransaction? = null
}

private fun Map<String, Any?>.keepOnlyNonNullValues(): Map<String, Any> {
    val result = mutableMapOf<String, Any>()
    for (entry in this) {
        val value = entry.value
        if (value != null) {
            result[entry.key] = value
        }
    }
    return result
}

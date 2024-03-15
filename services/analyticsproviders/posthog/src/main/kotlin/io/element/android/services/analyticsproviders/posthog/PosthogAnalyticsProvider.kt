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

package io.element.android.services.analyticsproviders.posthog

import com.posthog.PostHogInterface
import com.squareup.anvil.annotations.ContributesMultibinding
import im.vector.app.features.analytics.itf.VectorAnalyticsEvent
import im.vector.app.features.analytics.itf.VectorAnalyticsScreen
import im.vector.app.features.analytics.plan.Error
import im.vector.app.features.analytics.plan.UserProperties
import io.element.android.libraries.di.AppScope
import io.element.android.services.analyticsproviders.api.AnalyticsProvider
import io.element.android.services.analyticsproviders.posthog.log.analyticsTag
import timber.log.Timber
import javax.inject.Inject

// private val REUSE_EXISTING_ID: String? = null
// private val IGNORED_OPTIONS: Options? = null

@ContributesMultibinding(AppScope::class)
class PosthogAnalyticsProvider @Inject constructor(
    private val postHogFactory: PostHogFactory,
) : AnalyticsProvider {
    override val name = "Posthog"

    private var posthog: PostHogInterface? = null
    private var analyticsId: String? = null

    override fun init() {
        posthog = createPosthog()
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
        posthog?.capture(
            event = event.getName(),
            properties = event.getProperties()?.keepOnlyNonNullValues().withExtraProperties(),
        )
    }

    override fun screen(screen: VectorAnalyticsScreen) {
        posthog?.screen(
            screenTitle = screen.getName(),
            properties = screen.getProperties().withExtraProperties(),
        )
    }

    override fun updateUserProperties(userProperties: UserProperties) {
//        posthog?.identify(
//            REUSE_EXISTING_ID, userProperties.getProperties()?.toPostHogUserProperties(),
//            IGNORED_OPTIONS
//        )
    }

    override fun trackError(throwable: Throwable) {
        // Not implemented
    }

    private fun createPosthog(): PostHogInterface = postHogFactory.createPosthog()

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

/**
 * Properties which will be added to all Events and Screens.
 */
private val extraProperties: Map<String, Any> = mapOf(
    "cryptoSDK" to Error.CryptoSDK.Rust
)

private fun Map<String, Any>?.withExtraProperties(): Map<String, Any> {
    return orEmpty() + extraProperties
}

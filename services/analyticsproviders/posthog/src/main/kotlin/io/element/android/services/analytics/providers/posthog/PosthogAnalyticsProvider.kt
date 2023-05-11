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

package io.element.android.services.analytics.providers.posthog

import com.posthog.android.Options
import com.posthog.android.PostHog
import com.posthog.android.Properties
import com.squareup.anvil.annotations.ContributesMultibinding
import im.vector.app.features.analytics.itf.VectorAnalyticsEvent
import im.vector.app.features.analytics.itf.VectorAnalyticsScreen
import im.vector.app.features.analytics.plan.UserProperties
import io.element.android.libraries.di.AppScope
import io.element.android.services.analytics.providers.api.AnalyticsProvider
import io.element.android.services.analytics.providers.api.Distributor
import io.element.android.services.analytics.providers.posthog.impl.PostHogFactory
import io.element.android.services.analytics.providers.posthog.log.analyticsTag
import timber.log.Timber
import javax.inject.Inject

private val REUSE_EXISTING_ID: String? = null
private val IGNORED_OPTIONS: Options? = null

@ContributesMultibinding(AppScope::class)
class PosthogAnalyticsProvider @Inject constructor(
    private val postHogFactory: PostHogFactory,
): AnalyticsProvider {
    override val index = PosthogConfig.index
    override val name = PosthogConfig.name

    private var posthog: PostHog? = null
    private var analyticsId: String? = null

    override fun getDistributors(): List<Distributor> {
        return listOf(
            Distributor(
                "Posthog",
                "Posthog"
            )
        )
    }

    override suspend fun init() {
        posthog = createPosthog()
        posthog?.optOut(false)
        identifyPostHog()
    }

    override fun stop() {
        // When opting out, ensure that the queue is flushed first, or it will be flushed later (after user has revoked consent)
        posthog?.flush()
        posthog?.optOut(true)
        posthog?.shutdown()
        posthog = null
        analyticsId = null
    }

    override suspend fun troubleshoot(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun capture(event: VectorAnalyticsEvent) {
        posthog?.capture(event.getName(), event.getProperties()?.toPostHogProperties())
    }

    override fun screen(screen: VectorAnalyticsScreen) {
        posthog?.screen(screen.getName(), screen.getProperties()?.toPostHogProperties())
    }

    override fun updateUserProperties(userProperties: UserProperties) {
        posthog?.identify(
            REUSE_EXISTING_ID, userProperties.getProperties()?.toPostHogUserProperties(),
            IGNORED_OPTIONS
        )
    }

    override fun trackError(throwable: Throwable) {
        TODO("Not yet implemented")
    }

    private fun createPosthog(): PostHog = postHogFactory.createPosthog()

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

    private fun Map<String, Any?>?.toPostHogProperties(): Properties? {
        if (this == null) return null

        return Properties().apply {
            putAll(this@toPostHogProperties)
        }
    }

    /**
     * We avoid sending nulls as part of the UserProperties as this will reset the values across all devices.
     * The UserProperties event has nullable properties to allow for clients to opt in.
     */
    private fun Map<String, Any?>.toPostHogUserProperties(): Properties {
        return Properties().apply {
            putAll(this@toPostHogUserProperties.filter { it.value != null })
        }
    }
}

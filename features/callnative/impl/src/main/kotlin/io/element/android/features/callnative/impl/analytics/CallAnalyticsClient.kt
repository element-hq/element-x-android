/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.callnative.impl.analytics

import android.content.Context
import com.posthog.PostHogInterface
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.widget.CallAnalyticCredentialsProvider

/** Where the call's analytics events are sent, while the user consents. */
interface CallAnalyticsClient {
    fun capture(event: String, properties: Map<String, Any>)

    /** Sends what is queued, then stops for good. */
    fun close()
}

fun interface CallAnalyticsClientFactory {
    /** Null when the build has no analytics project for calls. */
    fun create(): CallAnalyticsClient?
}

/**
 * A PostHog client of its own over the embedded Element Call's credentials (`ELEMENT_CALL_POSTHOG_*`), not Element X's
 * main PostHog project: the native and the embedded call share their graphs, which the app's events are not part of.
 */
@ContributesBinding(AppScope::class)
class PostHogCallAnalyticsClientFactory(
    @ApplicationContext private val context: Context,
    private val buildMeta: BuildMeta,
    private val credentials: CallAnalyticCredentialsProvider,
) : CallAnalyticsClientFactory {
    override fun create(): CallAnalyticsClient? {
        val host = credentials.posthogApiHost ?: return null
        val apiKey = credentials.posthogApiKey ?: return null
        val posthog = PostHogAndroid.with(
            context,
            PostHogAndroidConfig(
                projectToken = apiKey,
                host = host,
                captureApplicationLifecycleEvents = false,
                captureDeepLinks = false,
                captureScreenViews = false,
                capturePushNotificationSubscriptions = false,
                capturePushNotificationOpened = false,
            ).also {
                it.debug = buildMeta.isDebuggable
                it.sendFeatureFlagEvent = false
            }
        )
        posthog.optIn()
        // The id the embedded call identifies with (its `posthogUserId` parameter); anonymous without one.
        credentials.posthogUserId?.let(posthog::identify)
        return PostHogCallAnalyticsClient(posthog)
    }
}

private class PostHogCallAnalyticsClient(private val posthog: PostHogInterface) : CallAnalyticsClient {
    override fun capture(event: String, properties: Map<String, Any>) {
        posthog.capture(event = event, properties = properties)
    }

    override fun close() {
        // Flushed first, or the queue would be sent later, after the user revoked consent.
        posthog.flush()
        posthog.optOut()
        posthog.close()
    }
}

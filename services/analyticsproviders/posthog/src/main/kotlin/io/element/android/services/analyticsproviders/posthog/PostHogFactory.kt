/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.services.analyticsproviders.posthog

import android.content.Context
import com.posthog.PostHogInterface
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig
import io.element.android.libraries.core.extensions.isElement
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.ApplicationContext
import javax.inject.Inject

class PostHogFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val buildMeta: BuildMeta,
    private val posthogEndpointConfigProvider: PosthogEndpointConfigProvider,
) {
    fun createPosthog(): PostHogInterface? {
        if (!buildMeta.isElement()) return null
        val endpoint = posthogEndpointConfigProvider.provide()
        return PostHogAndroid.with(
            context,
            PostHogAndroidConfig(
                apiKey = endpoint.apiKey,
                host = endpoint.host,
                captureApplicationLifecycleEvents = false,
                captureDeepLinks = false,
                captureScreenViews = false,
            ).also {
                it.debug = buildMeta.isDebuggable
                it.sendFeatureFlagEvent = false
            }
        )
    }
}

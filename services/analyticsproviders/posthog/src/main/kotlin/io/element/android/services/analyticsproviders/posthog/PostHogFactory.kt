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

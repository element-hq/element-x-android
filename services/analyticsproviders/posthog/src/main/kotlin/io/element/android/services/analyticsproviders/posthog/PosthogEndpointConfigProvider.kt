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

import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import javax.inject.Inject

class PosthogEndpointConfigProvider @Inject constructor(
    private val buildMeta: BuildMeta,
) {
    fun provide(): PosthogEndpointConfig {
        return when (buildMeta.buildType) {
            BuildType.RELEASE -> PosthogEndpointConfig(
                host = "https://posthog.element.io",
                apiKey = "phc_Jzsm6DTm6V2705zeU5dcNvQDlonOR68XvX2sh1sEOHO",
            )
            BuildType.NIGHTLY,
            BuildType.DEBUG -> PosthogEndpointConfig(
                host = "https://posthog.element.dev",
                apiKey = "phc_VtA1L35nw3aeAtHIx1ayrGdzGkss7k1xINeXcoIQzXN",
            )
        }
    }
}

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analyticsproviders.posthog

import dev.zacsweers.metro.Inject
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.libraries.core.extensions.isElement
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType

@Inject
class PosthogEndpointConfigProvider(
    private val buildMeta: BuildMeta,
    private val enterpriseService: EnterpriseService,
) {
    fun provide(): PosthogEndpointConfig? {
        return if (enterpriseService.isEnterpriseBuild) {
            PosthogEndpointConfig(
                host = BuildConfig.POSTHOG_HOST,
                apiKey = BuildConfig.POSTHOG_APIKEY,
            ).takeIf {
                // Note that if the config is invalid, this module will not be included in the build.
                // So the configuration should be always valid.
                it.isValid
            }
        } else if (buildMeta.isElement()) {
            when (buildMeta.buildType) {
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
        } else {
            null
        }
    }
}

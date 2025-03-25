/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

import config.AnalyticsConfig
import config.BuildTimeConfig
import config.PushProvidersConfig

object ModulesConfig {
    val pushProvidersConfig = PushProvidersConfig(
        includeFirebase = true,
        includeUnifiedPush = true,
    )

    val analyticsConfig: AnalyticsConfig = if (isEnterpriseBuild) {
        // Is Posthog configuration available?
        val withPosthog = BuildTimeConfig.SERVICES_POSTHOG_APIKEY.isNullOrEmpty().not() &&
            BuildTimeConfig.SERVICES_POSTHOG_HOST.isNullOrEmpty().not()
        // Is Sentry configuration available?
        val withSentry = BuildTimeConfig.SERVICES_SENTRY_DSN.isNullOrEmpty().not()
        if (withPosthog || withSentry) {
            AnalyticsConfig.Enabled(
                withPosthog = withPosthog,
                withSentry = withSentry,
            )
        } else {
            AnalyticsConfig.Disabled
        }
    } else {
        AnalyticsConfig.Enabled(
            withPosthog = true,
            withSentry = true,
        )
    }
}

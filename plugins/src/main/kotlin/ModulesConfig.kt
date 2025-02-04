/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

import config.AnalyticsConfig
import config.PushProvidersConfig

object ModulesConfig {
    val pushProvidersConfig = PushProvidersConfig(
        includeFirebase = true,
        includeUnifiedPush = true,
    )

    val analyticsConfig: AnalyticsConfig = AnalyticsConfig.Enabled(
        withPosthog = true,
        withSentry = true,
    )
}

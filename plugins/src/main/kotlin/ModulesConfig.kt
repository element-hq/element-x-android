/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

import config.AnalyticsConfig

object ModulesConfig {
    val analyticsConfig: AnalyticsConfig = AnalyticsConfig.Enabled(
        withPosthog = true,
        withSentry = true
    )
}

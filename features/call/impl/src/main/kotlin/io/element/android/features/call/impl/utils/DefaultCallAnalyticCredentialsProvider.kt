/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.utils

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.features.call.impl.BuildConfig
import io.element.android.libraries.matrix.api.widget.CallAnalyticCredentialsProvider

@ContributesBinding(AppScope::class)
@Inject
class DefaultCallAnalyticCredentialsProvider : CallAnalyticCredentialsProvider {
    override val posthogUserId: String? = BuildConfig.POSTHOG_USER_ID.takeIf { it.isNotBlank() }
    override val posthogApiHost: String? = BuildConfig.POSTHOG_API_HOST.takeIf { it.isNotBlank() }
    override val posthogApiKey: String? = BuildConfig.POSTHOG_API_KEY.takeIf { it.isNotBlank() }
    override val rageshakeSubmitUrl: String? = BuildConfig.RAGESHAKE_URL.takeIf { it.isNotBlank() }
    override val sentryDsn: String? = BuildConfig.SENTRY_DSN.takeIf { it.isNotBlank() }
}

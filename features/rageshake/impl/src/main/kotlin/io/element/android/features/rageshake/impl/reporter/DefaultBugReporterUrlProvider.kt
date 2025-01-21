/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.reporter

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.appconfig.RageshakeConfig
import io.element.android.libraries.di.AppScope
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultBugReporterUrlProvider @Inject constructor() : BugReporterUrlProvider {
    override fun provide(): HttpUrl {
        return RageshakeConfig.BUG_REPORT_URL.toHttpUrl()
    }
}

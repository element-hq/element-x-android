/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.reporter

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.appconfig.RageshakeConfig
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

fun interface BugReportAppNameProvider {
    fun provide(): String
}

@ContributesBinding(AppScope::class)
class DefaultBugReportAppNameProvider @Inject constructor() : BugReportAppNameProvider {
    override fun provide(): String = RageshakeConfig.BUG_REPORT_APP_NAME
}

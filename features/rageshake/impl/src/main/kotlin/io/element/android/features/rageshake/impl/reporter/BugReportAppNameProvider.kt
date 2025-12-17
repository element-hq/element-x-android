/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.reporter

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.appconfig.RageshakeConfig

fun interface BugReportAppNameProvider {
    fun provide(): String
}

@ContributesBinding(AppScope::class)
class DefaultBugReportAppNameProvider : BugReportAppNameProvider {
    override fun provide(): String = RageshakeConfig.BUG_REPORT_APP_NAME
}

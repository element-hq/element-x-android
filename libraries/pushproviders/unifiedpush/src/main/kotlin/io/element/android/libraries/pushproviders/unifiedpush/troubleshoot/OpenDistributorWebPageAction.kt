/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush.troubleshoot

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.androidutils.system.openUrlInExternalApp
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.pushproviders.unifiedpush.UnifiedPushConfig

interface OpenDistributorWebPageAction {
    fun execute()
}

@ContributesBinding(AppScope::class)
class DefaultOpenDistributorWebPageAction(
    @ApplicationContext private val context: Context,
) : OpenDistributorWebPageAction {
    override fun execute() {
        // Open the distributor download page
        context.openUrlInExternalApp(
            url = UnifiedPushConfig.UNIFIED_PUSH_DISTRIBUTORS_URL,
        )
    }
}

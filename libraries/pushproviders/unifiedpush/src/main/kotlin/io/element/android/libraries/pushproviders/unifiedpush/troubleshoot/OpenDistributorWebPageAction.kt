/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush.troubleshoot

import android.content.Context
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.androidutils.system.openUrlInExternalApp
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.pushproviders.unifiedpush.UnifiedPushConfig
import javax.inject.Inject

interface OpenDistributorWebPageAction {
    fun execute()
}

@ContributesBinding(AppScope::class)
class DefaultOpenDistributorWebPageAction @Inject constructor(
    @ApplicationContext private val context: Context,
) : OpenDistributorWebPageAction {
    override fun execute() {
        // Open the distributor download page
        context.openUrlInExternalApp(
            url = UnifiedPushConfig.UNIFIED_PUSH_DISTRIBUTORS_URL,
        )
    }
}

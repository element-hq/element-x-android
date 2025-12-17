/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.androidutils.system.getApplicationLabel
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.pushproviders.api.Distributor
import org.unifiedpush.android.connector.UnifiedPush

interface UnifiedPushDistributorProvider {
    fun getDistributors(): List<Distributor>
}

@ContributesBinding(AppScope::class)
class DefaultUnifiedPushDistributorProvider(
    @ApplicationContext private val context: Context,
) : UnifiedPushDistributorProvider {
    override fun getDistributors(): List<Distributor> {
        val distributors = UnifiedPush.getDistributors(context)
        return distributors.mapNotNull {
            if (it == context.packageName) {
                // Exclude self
                null
            } else {
                Distributor(it, context.getApplicationLabel(it))
            }
        }
    }
}

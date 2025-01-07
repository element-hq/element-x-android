/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import android.content.Context
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.androidutils.system.getApplicationLabel
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.pushproviders.api.Distributor
import org.unifiedpush.android.connector.UnifiedPush
import javax.inject.Inject

interface UnifiedPushDistributorProvider {
    fun getDistributors(): List<Distributor>
}

@ContributesBinding(AppScope::class)
class DefaultUnifiedPushDistributorProvider @Inject constructor(
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

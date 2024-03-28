/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

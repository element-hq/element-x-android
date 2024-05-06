/*
 * Copyright (c) 2023 New Vector Ltd
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
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.pushproviders.api.Distributor
import org.unifiedpush.android.connector.UnifiedPush
import javax.inject.Inject

class RegisterUnifiedPushUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun execute(distributor: Distributor, clientSecret: String): Result<Unit> {
        UnifiedPush.saveDistributor(context, distributor.value)
        // This will trigger the callback
        // VectorUnifiedPushMessagingReceiver.onNewEndpoint
        UnifiedPush.registerApp(context = context, instance = clientSecret)
        return Result.success(Unit)
    }
}

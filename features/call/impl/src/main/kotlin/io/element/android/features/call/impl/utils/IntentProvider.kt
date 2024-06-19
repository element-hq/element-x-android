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

package io.element.android.features.call.impl.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.PendingIntentCompat
import io.element.android.features.call.api.CallType
import io.element.android.features.call.impl.DefaultElementCallEntryPoint
import io.element.android.features.call.impl.ui.ElementCallActivity

internal object IntentProvider {
    fun createIntent(context: Context, callType: CallType): Intent = Intent(context, ElementCallActivity::class.java).apply {
        putExtra(DefaultElementCallEntryPoint.EXTRA_CALL_TYPE, callType)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
    }

    fun getPendingIntent(context: Context, callType: CallType): PendingIntent {
        return PendingIntentCompat.getActivity(
            context,
            DefaultElementCallEntryPoint.REQUEST_CODE,
            createIntent(context, callType),
            0,
            false
        )!!
    }
}

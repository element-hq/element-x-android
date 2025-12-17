/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
            PendingIntent.FLAG_CANCEL_CURRENT,
            false
        )!!
    }
}

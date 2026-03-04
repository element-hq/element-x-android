/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.impl

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.share.api.OnSharedData
import io.element.android.features.share.api.ShareIntentData
import io.element.android.libraries.di.annotations.ApplicationContext
import timber.log.Timber
import kotlin.collections.forEach

@ContributesBinding(AppScope::class)
class DefaultOnSharedData(
    @ApplicationContext private val context: Context,
) : OnSharedData {
    override fun invoke(data: ShareIntentData) {
        when (data) {
            is ShareIntentData.PlainText -> {
                // No-op, there is nothing to do for plain text intents.
            }
            is ShareIntentData.Uris -> {
                revokeUriPermissions(data.uris.map { it.uri })
            }
        }
    }

    private fun revokeUriPermissions(uris: List<Uri>) {
        uris.forEach { uri ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.revokeUriPermission(context.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    context.revokeUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } catch (e: Exception) {
                Timber.w(e, "Unable to revoke Uri permission")
            }
        }
    }
}

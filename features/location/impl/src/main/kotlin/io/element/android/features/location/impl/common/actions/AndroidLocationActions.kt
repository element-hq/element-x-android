/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.location.impl.common.actions

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.location.api.Location
import io.element.android.libraries.androidutils.system.openAppSettingsPage
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class AndroidLocationActions @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationActions {
    override fun share(location: Location, label: String?) {
        runCatching {
            val uri = Uri.parse(buildUrl(location, label))
            val showMapsIntent = Intent(Intent.ACTION_VIEW).setData(uri)
            val chooserIntent = Intent.createChooser(showMapsIntent, null)
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
        }.onSuccess {
            Timber.v("Open location succeed")
        }.onFailure {
            Timber.e(it, "Open location failed")
        }
    }

    override fun openSettings() {
        context.openAppSettingsPage()
    }
}

@VisibleForTesting
internal fun buildUrl(
    location: Location,
    label: String?,
    urlEncoder: (String) -> String = Uri::encode
): String {
    // Ref: https://developer.android.com/guide/components/intents-common#ViewMap
    val base = "geo:0,0?q=%.6f,%.6f".format(location.lat, location.lon)
    return if (label == null) {
        base
    } else {
        "%s (%s)".format(base, urlEncoder(label))
    }
}

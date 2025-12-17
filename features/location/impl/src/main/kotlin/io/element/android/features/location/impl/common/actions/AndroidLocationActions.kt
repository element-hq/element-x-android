/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.actions

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.core.net.toUri
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.location.api.Location
import io.element.android.libraries.androidutils.system.openAppSettingsPage
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.annotations.ApplicationContext
import timber.log.Timber
import java.util.Locale

@ContributesBinding(AppScope::class)
class AndroidLocationActions(
    @ApplicationContext private val context: Context
) : LocationActions {
    override fun share(location: Location, label: String?) {
        runCatchingExceptions {
            val uri = buildUrl(location, label).toUri()
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

// Ref: https://developer.android.com/guide/components/intents-common#ViewMap
@VisibleForTesting
internal fun buildUrl(
    location: Location,
    label: String?,
    urlEncoder: (String) -> String = Uri::encode
): String {
    // This is needed so the coordinates are formatted with a dot as decimal separator
    val locale = Locale.ENGLISH
    return "geo:0,0?q=%.6f,%.6f (%s)".format(locale, location.lat, location.lon, urlEncoder(label.orEmpty()))
}

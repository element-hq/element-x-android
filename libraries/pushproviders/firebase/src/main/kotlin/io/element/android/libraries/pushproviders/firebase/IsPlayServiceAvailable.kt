/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

interface IsPlayServiceAvailable {
    fun isAvailable(): Boolean
}

fun IsPlayServiceAvailable.checkAvailableOrThrow() {
    if (!isAvailable()) {
        throw Exception("No valid Google Play Services found. Cannot use FCM.").also(Timber::e)
    }
}

@ContributesBinding(AppScope::class)
class DefaultIsPlayServiceAvailable @Inject constructor(
    @ApplicationContext private val context: Context,
) : IsPlayServiceAvailable {
    override fun isAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailabilityLight.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(context)
        return if (resultCode == ConnectionResult.SUCCESS) {
            Timber.d("Google Play Services is available")
            true
        } else {
            Timber.w("Google Play Services is not available")
            false
        }
    }
}

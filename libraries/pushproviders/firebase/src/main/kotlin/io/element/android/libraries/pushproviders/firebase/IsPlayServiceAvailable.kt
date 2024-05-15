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

/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl

import android.content.Context
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.location.api.LocationService
import io.element.android.features.location.api.R
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultLocationService @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocationService {
    override fun isServiceAvailable(): Boolean {
        return context.getString(R.string.maptiler_api_key).isNotEmpty()
    }
}

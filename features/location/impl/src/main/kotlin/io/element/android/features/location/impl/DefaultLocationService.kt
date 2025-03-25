/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.location.api.BuildConfig
import io.element.android.features.location.api.LocationService
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultLocationService @Inject constructor() : LocationService {
    override fun isServiceAvailable(): Boolean {
        return BuildConfig.MAPTILER_API_KEY.isNotEmpty()
    }
}

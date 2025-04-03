/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.location.api.LocationService
import io.element.android.features.location.api.R
import io.element.android.libraries.di.AppScope
import io.element.android.services.toolbox.api.strings.StringProvider
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultLocationService @Inject constructor(
    private val stringProvider: StringProvider,
) : LocationService {
    override fun isServiceAvailable(): Boolean {
        return stringProvider.getString(R.string.maptiler_api_key).isNotEmpty()
    }
}

/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.appconfig.RageshakeConfig
import io.element.android.appconfig.isEnabled
import io.element.android.features.rageshake.api.RageshakeFeatureAvailability
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultRageshakeFeatureAvailability @Inject constructor() : RageshakeFeatureAvailability {
    override fun isAvailable(): Boolean {
        return RageshakeConfig.isEnabled
    }
}

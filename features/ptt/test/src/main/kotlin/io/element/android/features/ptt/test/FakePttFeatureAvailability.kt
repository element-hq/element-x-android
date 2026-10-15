/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.test

import io.element.android.features.ptt.api.PttFeatureAvailability
import kotlinx.coroutines.flow.MutableStateFlow

class FakePttFeatureAvailability(enabled: Boolean = false) : PttFeatureAvailability {
    override val isAvailable = MutableStateFlow(enabled)
}

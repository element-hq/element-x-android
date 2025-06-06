/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.api.battery

import android.app.Activity

data class BatteryOptimizationState(
    val shouldDisplayBanner: Boolean,
    val dismiss: () -> Unit,
    val openSettings: (Activity?) -> Unit,
)

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.battery

import io.element.android.tests.testutils.lambda.lambdaError

class FakeBatteryOptimization(
    var isIgnoringBatteryOptimizationsResult: Boolean = false,
    private val requestDisablingBatteryOptimizationResult: () -> Boolean = { lambdaError() }
) : BatteryOptimization {
    override fun isIgnoringBatteryOptimizations(): Boolean {
        return isIgnoringBatteryOptimizationsResult
    }

    override fun requestDisablingBatteryOptimization(): Boolean {
        return requestDisablingBatteryOptimizationResult()
    }
}

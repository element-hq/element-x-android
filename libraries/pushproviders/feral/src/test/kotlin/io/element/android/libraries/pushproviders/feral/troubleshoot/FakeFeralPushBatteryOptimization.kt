/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.feral.troubleshoot

class FakeFeralPushBatteryOptimization(
    var ignoring: Boolean = true,
    private val requestResult: () -> Boolean = { true },
) : FeralPushBatteryOptimization {
    var requestCalls = 0

    override fun isIgnoringBatteryOptimizations(): Boolean = ignoring

    override fun requestIgnoringBatteryOptimizations(): Boolean {
        requestCalls++
        return requestResult()
    }
}

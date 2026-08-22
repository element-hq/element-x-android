/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.feral

import io.element.android.libraries.pushproviders.feral.service.FeralPushServiceController
import kotlinx.coroutines.Job

class FakeFeralPushServiceController : FeralPushServiceController {
    var ensureStartedCalls = 0
    var startIfRegisteredCalls = 0
    var stopCalls = 0

    override fun ensureStarted() {
        ensureStartedCalls++
    }

    override fun startIfRegistered(): Job {
        startIfRegisteredCalls++
        return Job().apply { complete() }
    }

    override fun stop() {
        stopCalls++
    }
}

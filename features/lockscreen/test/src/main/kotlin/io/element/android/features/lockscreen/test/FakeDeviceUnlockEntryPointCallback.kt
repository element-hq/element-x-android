/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.test

import io.element.android.features.lockscreen.api.DeviceUnlockEntryPoint
import io.element.android.tests.testutils.EnsureNeverCalled

class FakeDeviceUnlockEntryPointCallback(
    private val onCancelLambda: () -> Unit = EnsureNeverCalled(),
    private val onUnlockedLambda: () -> Unit = EnsureNeverCalled(),
) : DeviceUnlockEntryPoint.Callback {
    override fun onCancel() = onCancelLambda()
    override fun onUnlock() = onUnlockedLambda()
}

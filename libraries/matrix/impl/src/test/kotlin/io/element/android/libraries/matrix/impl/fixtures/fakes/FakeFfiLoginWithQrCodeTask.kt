/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import org.matrix.rustcomponents.sdk.LoginWithQrCodeTask
import org.matrix.rustcomponents.sdk.NoHandle

class FakeFfiLoginWithQrCodeTask(
    private val waitResult: suspend () -> Unit = {},
    private val cancelResult: () -> Unit = {},
) : LoginWithQrCodeTask(NoHandle) {
    override suspend fun wait() = waitResult()
    override fun cancel() = cancelResult()
    override fun close() = Unit
}

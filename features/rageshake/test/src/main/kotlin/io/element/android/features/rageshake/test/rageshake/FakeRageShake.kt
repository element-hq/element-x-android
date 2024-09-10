/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.rageshake.test.rageshake

import io.element.android.features.rageshake.api.rageshake.RageShake

class FakeRageShake(
    private var isAvailableValue: Boolean = true
) : RageShake {
    private var interceptor: (() -> Unit)? = null

    override fun isAvailable() = isAvailableValue

    override fun start(sensitivity: Float) {
    }

    override fun stop() {
    }

    override fun setSensitivity(sensitivity: Float) {
    }

    override fun setInterceptor(interceptor: (() -> Unit)?) {
        this.interceptor = interceptor
    }

    fun triggerPhoneRageshake() = interceptor?.invoke()
}

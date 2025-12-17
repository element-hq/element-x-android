/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.rageshake

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

/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.feral.connection

class FakeFeralPushSocket(
    val url: String,
    val listener: FeralPushSocketListener,
) : FeralPushSocket {
    var isCancelled = false
        private set

    override fun cancel() {
        isCancelled = true
    }
}

class FakeFeralPushSocketFactory : FeralPushSocketFactory {
    val sockets = mutableListOf<FakeFeralPushSocket>()

    val last: FakeFeralPushSocket get() = sockets.last()

    override fun open(url: String, listener: FeralPushSocketListener): FeralPushSocket {
        return FakeFeralPushSocket(url, listener).also { sockets += it }
    }
}

/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.api

/**
 * The write side of [CurrentCallService], for whoever is actually running a call.
 *
 * Separate from [CurrentCallService] because the two have opposite audiences: everything in the app
 * that wants to know whether we are in a call reads the service, while only a call implementation
 * writes. The WebView path reaches the implementation directly through `ActiveCallManager`, both
 * being in `features/call/impl`; the native path is in another module and would otherwise have to
 * depend on that impl to report a call it is running itself.
 */
interface CurrentCallTracker {
    fun onCallStarted(call: CurrentCall)

    fun onCallEnded()
}

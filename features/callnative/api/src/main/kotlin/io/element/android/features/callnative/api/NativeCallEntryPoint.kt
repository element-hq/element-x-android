/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.callnative.api

import io.element.android.features.call.api.CallData

/**
 * Starts a call on the native MatrixRTC stack instead of the Element Call WebView.
 *
 * Experimental: gated behind `FeatureFlags.NativeCall` and reached only through
 * [io.element.android.features.call.api.ElementCallEntryPoint], so nothing else in the app has to know
 * it exists.
 *
 * There is nothing behind this seam yet - the default implementation does nothing and says so. The
 * native stack that implements it is a separate library, added in a follow-up.
 */
interface NativeCallEntryPoint {
    fun startCall(callData: CallData)
}

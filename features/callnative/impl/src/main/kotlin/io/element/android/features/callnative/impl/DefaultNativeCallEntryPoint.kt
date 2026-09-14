/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.callnative.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.call.api.CallData
import io.element.android.features.callnative.api.NativeCallEntryPoint
import timber.log.Timber

/**
 * What answers [NativeCallEntryPoint] until the native MatrixRTC stack is wired in: nothing.
 *
 * The seam exists so that `DefaultElementCallEntryPoint` can fork on `FeatureFlags.NativeCall` today,
 * and so that adding the stack later is a change to this module alone. A user who turns the flag on
 * before then gets no call and a line in the log, which is the honest outcome - falling back to the
 * WebView would make the flag look broken rather than unimplemented.
 */
@ContributesBinding(AppScope::class)
class DefaultNativeCallEntryPoint : NativeCallEntryPoint {
    override fun startCall(callData: CallData) {
        Timber.w("Native call requested for ${callData.roomId} but no native call stack is present in this build")
    }
}

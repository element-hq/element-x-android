/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.callnative.impl

import android.app.Activity
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.call.impl.ElementCallPictureInPicture
import io.element.android.features.callnative.api.NativeCallPip

/**
 * Forwards the Activity's leave hint to whichever session has a call.
 *
 * App-scoped because `onUserLeaveHint` is, and it asks for the running call rather than a particular
 * session's: only one call runs at a time, and the Activity has no session in scope to name. Sessions
 * whose stack was never built are skipped, so asking cannot start one.
 */
@ContributesBinding(AppScope::class)
class DefaultNativeCallPip(
    private val controllers: ElementCallControllers,
) : NativeCallPip {
    override fun onUserLeaveHint(activity: Activity) {
        val controller = controllers.withRunningCall() ?: return
        ElementCallPictureInPicture.onUserLeaveHint(activity, controller)
    }
}

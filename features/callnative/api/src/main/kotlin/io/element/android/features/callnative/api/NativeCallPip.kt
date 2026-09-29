/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.callnative.api

import android.app.Activity

/**
 * The one part of picture-in-picture that an Activity has to do itself.
 *
 * Everything else is installed where the call is drawn, which is inside the logged-in content and so
 * knows which session's call it is. This does not: `onUserLeaveHint` is an Activity override, there is
 * no session in scope at that point, and it applies to whichever call happens to be running.
 *
 * Only Android 11 and below reach this. From Android 12 the system is told up front that the Activity
 * would like to shrink, which covers every way out of the app including the home gesture - the one
 * people actually use, and the one this hook never sees.
 */
interface NativeCallPip {
    /** Call from the host Activity's `onUserLeaveHint()` override. */
    fun onUserLeaveHint(activity: Activity)
}

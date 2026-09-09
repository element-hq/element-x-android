/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.api

import android.app.Activity

/**
 * A hook that is called during app startup, from the `MainActivity`.
 *
 * Note that the hook is run in the background: it will not block startup of the app.
 */
fun interface AppStartupHook {
    /** @param activity The activity which is active while the app starts (i.e. the `MainActivity`). */
    suspend fun onAppStartup(activity: Activity)
}

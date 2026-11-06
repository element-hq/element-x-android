/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.callnative.impl

import io.element.android.call.api.ElementCallController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import timber.log.Timber

/**
 * Tells the call when the session's client goes offline and when it is back (element-call-android README, host
 * requirement 6), so the call says "your server is unreachable" at once and retries its sends as soon as it can.
 *
 * [isOnline] is `SyncService.isOnline`, the same signal as the app's offline banner. The client runs in the SDK's
 * offline mode: it goes offline on a request failing for want of the server, and comes back only once a request to
 * it has succeeded again, which is exactly the transition the call wants. Sync merely starting or stopping does not
 * move it.
 *
 * Suspends for as long as it is collected, which is the session's life.
 */
internal suspend fun ElementCallController.followConnectivity(isOnline: Flow<Boolean>) {
    isOnline
        .distinctUntilChanged()
        // Only the way out of offline is news: the core assumes it is online until told otherwise.
        .dropWhile { it }
        .collect {
            Timber.i("NativeCall: client online=$it")
            reportConnectivity(it)
        }
}

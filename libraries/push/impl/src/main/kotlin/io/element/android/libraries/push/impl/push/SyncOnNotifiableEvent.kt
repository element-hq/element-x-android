/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import io.element.android.libraries.push.impl.db.PushRequest

/**
 * A helper that triggers an opportunistic sync when a notifiable event is received.
 *
 * When this happens, the app is in background but has internet connectivity, so we can trigger a sync to get
 * the latest state of the rooms that have new events.
 *
 * This is useful to avoid showing stale data when the user opens the app after receiving a push notification.
 */
fun interface SyncOnNotifiableEvent {
    suspend operator fun invoke(requests: List<PushRequest>)
}

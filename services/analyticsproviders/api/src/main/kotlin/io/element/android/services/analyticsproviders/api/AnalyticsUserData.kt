/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analyticsproviders.api

object AnalyticsUserData {
    const val HOMESERVER = "homeserver"

    const val STATE_STORE_SIZE = "state_store_size"
    const val EVENT_CACHE_SIZE = "event_cache_size"
    const val CRYPTO_STORE_SIZE = "crypto_store_size"
    const val MEDIA_STORE_SIZE = "media_store_size"

    const val FIRST_SYNC_STATE = "first_sync_state"
    const val TIMELINE_ITEM_COUNT = "timeline_item_count"
}

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.api

import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.flow.Flow

interface CacheService {
    /**
     * A flow of [SessionId], can let the app to know when the
     * cache has been cleared for a given session, for instance to restart the app.
     */
    val clearedCacheEventFlow: Flow<SessionId>
}

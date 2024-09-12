/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.preferences.api.store

import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.CoroutineScope

interface SessionPreferencesStoreFactory {
    fun get(sessionId: SessionId, sessionCoroutineScope: CoroutineScope): SessionPreferencesStore
    fun remove(sessionId: SessionId)
}

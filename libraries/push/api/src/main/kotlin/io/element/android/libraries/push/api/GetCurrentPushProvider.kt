/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.api

import io.element.android.libraries.matrix.api.core.SessionId

/**
 * Reads the name of the push provider a session is currently registered with.
 *
 * This is the low-level accessor used where depending on the whole push feature would be too much; prefer [PushService.getCurrentPushProvider] otherwise.
 */
interface GetCurrentPushProvider {
    /**
     * Returns the stored provider name, or `null` when the session has no push provider selected yet.
     *
     * @param sessionId the session to read the provider of.
     */
    suspend fun getCurrentPushProvider(sessionId: SessionId): String?
}

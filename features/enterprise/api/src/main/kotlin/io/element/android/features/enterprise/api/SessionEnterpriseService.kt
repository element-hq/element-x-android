/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.api

/**
 * The session-scoped counterpart of [EnterpriseService], answering the same kind of questions for the session it belongs to.
 *
 * [init] must have run before the other members return meaningful answers.
 */
interface SessionEnterpriseService {
    /** Loads the deployment configuration of this session; to be called once when the session starts. */
    suspend fun init()

    /** Whether Element Call may be used on this session, which a deployment can turn off. */
    suspend fun isElementCallAvailable(): Boolean

    /**
     * Rewrites the authentication server URL for this session; see [EnterpriseService.tweakMasUrl].
     *
     * @param url the URL to rewrite.
     */
    suspend fun tweakMasUrl(url: String): String

    /** Whether the homeserver forbids creating encrypted rooms, in which case the app must not offer the option. */
    suspend fun isEncryptionDisabledByHomeserver(): Boolean
}

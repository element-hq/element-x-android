/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.api

interface PushHandler {
    /**
     * Handle a push received from the provider.
     *
     * @param pushData the data of the push, containing the client secret and the push content.
     * @param providerInfo an identifier of the provider that sent the push, for logging and debugging purposes.
     * @return `true` if the push was handled successfully and is now enqueued for processing, false otherwise.
     */
    suspend fun handle(
        pushData: PushData,
        providerInfo: String,
    ): Boolean

    /**
     * Handle an invalid push received from the provider.
     */
    suspend fun handleInvalid(
        providerInfo: String,
        data: String,
    )
}

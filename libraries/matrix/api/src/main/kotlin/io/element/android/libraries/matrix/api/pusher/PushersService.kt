/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.pusher

/**
 * Registers and removes the pusher of this session, i.e. the HTTP gateway the homeserver sends push notifications to.
 */
interface PushersService {
    /**
     * Registers or updates the pusher; calling it again with the same push key replaces the existing registration.
     *
     * @param setHttpPusherData the gateway URL, the push key and the other details the homeserver needs to reach the device.
     */
    suspend fun setHttpPusher(setHttpPusherData: SetHttpPusherData): Result<Unit>

    /**
     * Removes the pusher, so the homeserver stops sending push notifications to this device.
     *
     * @param unsetHttpPusherData the push key and app id identifying the registration to remove.
     */
    suspend fun unsetHttpPusher(unsetHttpPusherData: UnsetHttpPusherData): Result<Unit>
}

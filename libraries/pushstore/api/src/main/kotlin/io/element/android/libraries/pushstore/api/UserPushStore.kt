/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushstore.api
import kotlinx.coroutines.flow.Flow

/**
 * Store data related to push about a user.
 */
interface UserPushStore {
    /** The push provider selected for this user, or `null` when none has been selected yet. */
    suspend fun getPushProviderName(): String?

    /**
     * @param value the name of the push provider to remember for this user.
     */
    suspend fun setPushProviderName(value: String)

    /** The push key last registered with the homeserver, used to detect that a re-registration is needed; `null` when never registered. */
    suspend fun getCurrentRegisteredPushKey(): String?

    /**
     * @param value the push key that has just been registered, or `null` to forget it after unregistering.
     */
    suspend fun setCurrentRegisteredPushKey(value: String?)

    /** Whether the user has enabled notifications on this device. */
    fun getNotificationEnabledForDevice(): Flow<Boolean>

    /**
     * @param enabled true to enable notifications on this device.
     */
    suspend fun setNotificationEnabledForDevice(enabled: Boolean)

    /** Whether the user has asked not to be warned about pusher registration failures. */
    fun ignoreRegistrationError(): Flow<Boolean>

    /**
     * @param ignore true to stop warning the user about registration failures.
     */
    suspend fun setIgnoreRegistrationError(ignore: Boolean)

    /**
     * Return true if Pin code is disabled, or if user set the settings to see full notification content.
     */
    fun useCompleteNotificationFormat(): Boolean

    /** Erases every push preference of this user, to be called when their session is removed. */
    suspend fun reset()
}

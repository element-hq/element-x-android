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
    suspend fun getPushProviderName(): String?
    suspend fun setPushProviderName(value: String)
    suspend fun getCurrentRegisteredPushKey(): String?
    suspend fun setCurrentRegisteredPushKey(value: String?)

    fun getNotificationEnabledForDevice(): Flow<Boolean>
    suspend fun setNotificationEnabledForDevice(enabled: Boolean)

    fun ignoreRegistrationError(): Flow<Boolean>
    suspend fun setIgnoreRegistrationError(ignore: Boolean)

    /**
     * Return true if Pin code is disabled, or if user set the settings to see full notification content.
     */
    fun useCompleteNotificationFormat(): Boolean

    suspend fun reset()
}

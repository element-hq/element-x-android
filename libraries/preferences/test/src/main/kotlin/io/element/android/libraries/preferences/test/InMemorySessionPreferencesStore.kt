/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.test

import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class InMemorySessionPreferencesStore(
    isSharePresenceEnabled: Boolean = true,
    isSendPublicReadReceiptsEnabled: Boolean = true,
    isRenderReadReceiptsEnabled: Boolean = true,
    isSendTypingNotificationsEnabled: Boolean = true,
    isRenderTypingNotificationsEnabled: Boolean = true,
    isSessionVerificationSkipped: Boolean = false,
    doesCompressMedia: Boolean = true,
) : SessionPreferencesStore {
    private val isSharePresenceEnabled = MutableStateFlow(isSharePresenceEnabled)
    private val isSendPublicReadReceiptsEnabled = MutableStateFlow(isSendPublicReadReceiptsEnabled)
    private val isRenderReadReceiptsEnabled = MutableStateFlow(isRenderReadReceiptsEnabled)
    private val isSendTypingNotificationsEnabled = MutableStateFlow(isSendTypingNotificationsEnabled)
    private val isRenderTypingNotificationsEnabled = MutableStateFlow(isRenderTypingNotificationsEnabled)
    private val isSessionVerificationSkipped = MutableStateFlow(isSessionVerificationSkipped)
    private val doesCompressMedia = MutableStateFlow(doesCompressMedia)
    var clearCallCount = 0
        private set

    override suspend fun setSharePresence(enabled: Boolean) {
        isSharePresenceEnabled.tryEmit(enabled)
    }

    override fun isSharePresenceEnabled(): Flow<Boolean> = isSharePresenceEnabled

    override suspend fun setSendPublicReadReceipts(enabled: Boolean) {
        isSendPublicReadReceiptsEnabled.tryEmit(enabled)
    }

    override fun isSendPublicReadReceiptsEnabled(): Flow<Boolean> = isSendPublicReadReceiptsEnabled

    override suspend fun setRenderReadReceipts(enabled: Boolean) {
        isRenderReadReceiptsEnabled.tryEmit(enabled)
    }

    override fun isRenderReadReceiptsEnabled(): Flow<Boolean> = isRenderReadReceiptsEnabled

    override suspend fun setSendTypingNotifications(enabled: Boolean) {
        isSendTypingNotificationsEnabled.tryEmit(enabled)
    }

    override fun isSendTypingNotificationsEnabled(): Flow<Boolean> = isSendTypingNotificationsEnabled

    override suspend fun setRenderTypingNotifications(enabled: Boolean) {
        isRenderTypingNotificationsEnabled.tryEmit(enabled)
    }

    override fun isRenderTypingNotificationsEnabled(): Flow<Boolean> = isRenderTypingNotificationsEnabled

    override suspend fun setSkipSessionVerification(skip: Boolean) {
        isSessionVerificationSkipped.tryEmit(skip)
    }

    override fun isSessionVerificationSkipped(): Flow<Boolean> {
        return isSessionVerificationSkipped
    }

    override suspend fun setCompressMedia(compress: Boolean) = doesCompressMedia.emit(compress)

    override fun doesCompressMedia(): Flow<Boolean> = doesCompressMedia

    override suspend fun clear() {
        clearCallCount++
        isSendPublicReadReceiptsEnabled.tryEmit(true)
    }
}

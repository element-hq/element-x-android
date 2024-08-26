/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    skinTone: String? = null,
) : SessionPreferencesStore {
    private val isSharePresenceEnabled = MutableStateFlow(isSharePresenceEnabled)
    private val isSendPublicReadReceiptsEnabled = MutableStateFlow(isSendPublicReadReceiptsEnabled)
    private val isRenderReadReceiptsEnabled = MutableStateFlow(isRenderReadReceiptsEnabled)
    private val isSendTypingNotificationsEnabled = MutableStateFlow(isSendTypingNotificationsEnabled)
    private val isRenderTypingNotificationsEnabled = MutableStateFlow(isRenderTypingNotificationsEnabled)
    private val isSessionVerificationSkipped = MutableStateFlow(isSessionVerificationSkipped)
    private val skinTone = MutableStateFlow(skinTone)
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

    override suspend fun setSkinTone(modifier: String?) {
        skinTone.tryEmit(modifier)
    }

    override fun getSkinTone(): Flow<String?> {
        return skinTone
    }

    override suspend fun clear() {
        clearCallCount++
        isSendPublicReadReceiptsEnabled.tryEmit(true)
    }
}

/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.libraries.push.impl.userpushstore

const val NOTIFICATION_METHOD_FIREBASE = "NOTIFICATION_METHOD_FIREBASE"
const val NOTIFICATION_METHOD_UNIFIEDPUSH = "NOTIFICATION_METHOD_UNIFIEDPUSH"

/**
 * Store data related to push about a user.
 */
interface UserPushStore {
    /**
     * [NOTIFICATION_METHOD_FIREBASE] or [NOTIFICATION_METHOD_UNIFIEDPUSH].
     */
    suspend fun getNotificationMethod(): String

    suspend fun setNotificationMethod(value: String)

    suspend fun getCurrentRegisteredPushKey(): String?

    suspend fun setCurrentRegisteredPushKey(value: String)

    suspend fun reset()
}

suspend fun UserPushStore.isFirebase(): Boolean = getNotificationMethod() == NOTIFICATION_METHOD_FIREBASE

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

package io.element.android.libraries.matrix.impl.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryService
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import kotlinx.coroutines.CoroutineScope

@Module
@ContributesTo(SessionScope::class)
object SessionMatrixModule {
    @Provides
    fun providesSessionVerificationService(matrixClient: MatrixClient): SessionVerificationService {
        return matrixClient.sessionVerificationService()
    }

    @Provides
    fun providesNotificationSettingsService(matrixClient: MatrixClient): NotificationSettingsService {
        return matrixClient.notificationSettingsService()
    }

    @Provides
    fun provideRoomMembershipObserver(matrixClient: MatrixClient): RoomMembershipObserver {
        return matrixClient.roomMembershipObserver()
    }

    @Provides
    fun providesRoomListService(matrixClient: MatrixClient): RoomListService {
        return matrixClient.roomListService
    }

    @Provides
    fun providesEncryptionService(matrixClient: MatrixClient): EncryptionService {
        return matrixClient.encryptionService()
    }

    @Provides
    fun provideMediaLoader(matrixClient: MatrixClient): MatrixMediaLoader {
        return matrixClient.mediaLoader
    }

    @SessionCoroutineScope
    @Provides
    fun provideSessionCoroutineScope(matrixClient: MatrixClient): CoroutineScope {
        return matrixClient.sessionCoroutineScope
    }

    @Provides
    fun providesRoomDirectoryService(matrixClient: MatrixClient): RoomDirectoryService {
        return matrixClient.roomDirectoryService()
    }
}

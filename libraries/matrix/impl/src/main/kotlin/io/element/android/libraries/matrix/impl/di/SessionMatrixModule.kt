/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.media.MediaPreviewService
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryService
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.spaces.SpaceService
import io.element.android.libraries.matrix.api.sync.SyncService
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import kotlinx.coroutines.CoroutineScope

@BindingContainer
@ContributesTo(SessionScope::class)
object SessionMatrixModule {
    @Provides
    fun providesSessionId(matrixClient: MatrixClient): SessionId {
        return matrixClient.sessionId
    }

    @Provides
    fun providesSessionVerificationService(matrixClient: MatrixClient): SessionVerificationService {
        return matrixClient.sessionVerificationService
    }

    @Provides
    fun providesNotificationSettingsService(matrixClient: MatrixClient): NotificationSettingsService {
        return matrixClient.notificationSettingsService
    }

    @Provides
    fun provideRoomMembershipObserver(matrixClient: MatrixClient): RoomMembershipObserver {
        return matrixClient.roomMembershipObserver
    }

    @Provides
    fun providesRoomListService(matrixClient: MatrixClient): RoomListService {
        return matrixClient.roomListService
    }

    @Provides
    fun providesSyncService(matrixClient: MatrixClient): SyncService {
        return matrixClient.syncService
    }

    @Provides
    fun providesEncryptionService(matrixClient: MatrixClient): EncryptionService {
        return matrixClient.encryptionService
    }

    @Provides
    fun providesMatrixMediaLoader(matrixClient: MatrixClient): MatrixMediaLoader {
        return matrixClient.matrixMediaLoader
    }

    @SessionCoroutineScope
    @Provides
    fun providesSessionCoroutineScope(matrixClient: MatrixClient): CoroutineScope {
        return matrixClient.sessionCoroutineScope
    }

    @Provides
    fun providesRoomDirectoryService(matrixClient: MatrixClient): RoomDirectoryService {
        return matrixClient.roomDirectoryService
    }

    @Provides
    fun providesMediaPreviewService(matrixClient: MatrixClient): MediaPreviewService {
        return matrixClient.mediaPreviewService
    }

    @Provides
    fun providesSpaceService(matrixClient: MatrixClient): SpaceService {
        return matrixClient.spaceService
    }
}

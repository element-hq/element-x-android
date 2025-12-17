/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:Suppress("DEPRECATION")

package io.element.android.appnav.loggedin

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.media.MediaPreviewConfig
import io.element.android.libraries.matrix.api.media.MediaPreviewValue
import io.element.android.libraries.matrix.test.media.FakeMediaPreviewService
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MediaPreviewConfigMigrationTest {
    @Test
    fun `when no local data exists, migration does nothing`() = runTest {
        val appPreferencesStore = InMemoryAppPreferencesStore()
        val mediaPreviewService = FakeMediaPreviewService(
            fetchMediaPreviewConfigResult = { Result.success(null) }
        )
        val migration = createMigration(appPreferencesStore, mediaPreviewService)

        migration().join()

        // Verify no calls were made to set server config
        // since there's nothing to migrate
    }

    @Test
    fun `when local data exists and server has config, clears local data`() = runTest {
        val appPreferencesStore = InMemoryAppPreferencesStore().apply {
            setHideInviteAvatars(true)
            setTimelineMediaPreviewValue(MediaPreviewValue.Private)
        }
        val serverConfig = MediaPreviewConfig(
            hideInviteAvatar = false,
            mediaPreviewValue = MediaPreviewValue.On
        )
        val mediaPreviewService = FakeMediaPreviewService(
            fetchMediaPreviewConfigResult = { Result.success(serverConfig) }
        )
        val migration = createMigration(appPreferencesStore, mediaPreviewService)

        migration().join()

        // Verify local data was cleared
        assertThat(appPreferencesStore.getHideInviteAvatarsFlow().first()).isNull()
        assertThat(appPreferencesStore.getTimelineMediaPreviewValueFlow().first()).isNull()
    }

    @Test
    fun `when local hideInviteAvatars exists and server has no config, migrates to server`() = runTest {
        val appPreferencesStore = InMemoryAppPreferencesStore().apply {
            setHideInviteAvatars(true)
        }
        var setHideInviteAvatarsValue: Boolean? = null
        val mediaPreviewService = FakeMediaPreviewService(
            fetchMediaPreviewConfigResult = { Result.success(null) },
            setHideInviteAvatarsResult = { value ->
                setHideInviteAvatarsValue = value
                Result.success(Unit)
            }
        )
        val migration = createMigration(appPreferencesStore, mediaPreviewService)

        migration().join()

        // Verify server was updated with local value
        assertThat(setHideInviteAvatarsValue).isTrue()
        // Verify local data was cleared
        assertThat(appPreferencesStore.getHideInviteAvatarsFlow().first()).isNull()
    }

    @Test
    fun `when local mediaPreviewValue exists and server has no config, migrates to server`() = runTest {
        val appPreferencesStore = InMemoryAppPreferencesStore().apply {
            setTimelineMediaPreviewValue(MediaPreviewValue.Private)
        }
        var setMediaPreviewValue: MediaPreviewValue? = null
        val mediaPreviewService = FakeMediaPreviewService(
            fetchMediaPreviewConfigResult = { Result.success(null) },
            setMediaPreviewValueResult = { value ->
                setMediaPreviewValue = value
                Result.success(Unit)
            }
        )
        val migration = createMigration(appPreferencesStore, mediaPreviewService)

        migration().join()

        // Verify server was updated with local value
        assertThat(setMediaPreviewValue).isEqualTo(MediaPreviewValue.Private)
        // Verify local data was cleared
        assertThat(appPreferencesStore.getTimelineMediaPreviewValueFlow().first()).isNull()
    }

    @Test
    fun `when both local values exist and server has no config, migrates both to server`() = runTest {
        val appPreferencesStore = InMemoryAppPreferencesStore().apply {
            setHideInviteAvatars(true)
            setTimelineMediaPreviewValue(MediaPreviewValue.Off)
        }
        var setHideInviteAvatarsValue: Boolean? = null
        var setMediaPreviewValue: MediaPreviewValue? = null
        val mediaPreviewService = FakeMediaPreviewService(
            fetchMediaPreviewConfigResult = { Result.success(null) },
            setHideInviteAvatarsResult = { value ->
                setHideInviteAvatarsValue = value
                Result.success(Unit)
            },
            setMediaPreviewValueResult = { value ->
                setMediaPreviewValue = value
                Result.success(Unit)
            }
        )
        val migration = createMigration(appPreferencesStore, mediaPreviewService)

        migration().join()

        // Verify server was updated with both local values
        assertThat(setHideInviteAvatarsValue).isTrue()
        assertThat(setMediaPreviewValue).isEqualTo(MediaPreviewValue.Off)
        // Verify local data was cleared
        assertThat(appPreferencesStore.getHideInviteAvatarsFlow().first()).isNull()
        assertThat(appPreferencesStore.getTimelineMediaPreviewValueFlow().first()).isNull()
    }

    @Test
    fun `when fetch config fails, migration does nothing`() = runTest {
        val appPreferencesStore = InMemoryAppPreferencesStore().apply {
            setHideInviteAvatars(true)
            setTimelineMediaPreviewValue(MediaPreviewValue.Private)
        }
        val mediaPreviewService = FakeMediaPreviewService(
            fetchMediaPreviewConfigResult = { Result.failure(Exception("Network error")) }
        )
        val migration = createMigration(appPreferencesStore, mediaPreviewService)

        migration().join()

        // Verify local data was not cleared since migration failed
        assertThat(appPreferencesStore.getHideInviteAvatarsFlow().first()).isTrue()
        assertThat(appPreferencesStore.getTimelineMediaPreviewValueFlow().first()).isEqualTo(MediaPreviewValue.Private)
    }

    private fun TestScope.createMigration(
        appPreferencesStore: InMemoryAppPreferencesStore,
        mediaPreviewService: FakeMediaPreviewService
    ) = MediaPreviewConfigMigration(
        mediaPreviewService = mediaPreviewService,
        appPreferencesStore = appPreferencesStore,
        sessionCoroutineScope = this
    )
}

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.loggedin

import dev.zacsweers.metro.Inject
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.media.MediaPreviewService
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * This migration is temporary, will be safe to remove after some time.
 * The goal is to set the server config if it's not set, and remove the local data.
 */
@Inject
class MediaPreviewConfigMigration(
    private val mediaPreviewService: MediaPreviewService,
    private val appPreferencesStore: AppPreferencesStore,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
) {
    @Suppress("DEPRECATION")
    operator fun invoke() = sessionCoroutineScope.launch {
        val hideInviteAvatars = appPreferencesStore.getHideInviteAvatarsFlow().first()
        val mediaPreviewValue = appPreferencesStore.getTimelineMediaPreviewValueFlow().first()
        if (hideInviteAvatars == null && mediaPreviewValue == null) {
            // No local data, abort.
            return@launch
        }
        mediaPreviewService
            .fetchMediaPreviewConfig()
            .onSuccess { config ->
                if (config != null) {
                    appPreferencesStore.setHideInviteAvatars(null)
                    appPreferencesStore.setTimelineMediaPreviewValue(null)
                } else {
                    if (hideInviteAvatars != null) {
                        mediaPreviewService.setHideInviteAvatars(hideInviteAvatars)
                        appPreferencesStore.setHideInviteAvatars(null)
                    }
                    if (mediaPreviewValue != null) {
                        mediaPreviewService.setMediaPreviewValue(mediaPreviewValue)
                        appPreferencesStore.setTimelineMediaPreviewValue(null)
                    }
                }
            }
            .onFailure {
                Timber.e(it, "Couldn't perform migration, failed to fetch media preview config.")
            }
    }
}

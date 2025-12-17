/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.announcement.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.Flow

interface AnnouncementService {
    suspend fun showAnnouncement(announcement: Announcement)

    suspend fun onAnnouncementDismissed(announcement: Announcement)

    fun announcementsToShowFlow(): Flow<List<Announcement>>

    /**
     * Use this composable to render the announcement UI in Fullscreen.
     */
    @Composable
    fun Render(
        modifier: Modifier,
    )
}

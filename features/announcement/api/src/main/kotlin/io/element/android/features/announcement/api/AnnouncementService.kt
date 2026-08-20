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

/**
 * Queues the full-screen announcements shown to the user once, such as the notice about a new feature.
 *
 * A dismissed announcement is remembered so it is not shown again.
 */
interface AnnouncementService {
    /**
     * Queues an announcement, unless the user has already dismissed it.
     *
     * @param announcement the announcement to show.
     */
    suspend fun showAnnouncement(announcement: Announcement)

    /**
     * Records that the user dismissed an announcement, so it is not queued again.
     *
     * @param announcement the announcement that was dismissed.
     */
    suspend fun onAnnouncementDismissed(announcement: Announcement)

    /** The announcements still waiting to be shown, in the order they should appear. */
    fun announcementsToShowFlow(): Flow<List<Announcement>>

    /**
     * Use this composable to render the announcement UI in Fullscreen.
     * Draws nothing while there is no announcement to show.
     *
     * @param modifier layout modifier for the container.
     */
    @Composable
    fun Render(
        modifier: Modifier,
    )
}

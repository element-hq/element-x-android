/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.announcement.impl.store

import io.element.android.features.announcement.api.Announcement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryAnnouncementStore(
    initialSpaceAnnouncementStatus: AnnouncementStatus = AnnouncementStatus.NeverShown,
    initialNewNotificationSoundAnnouncementStatus: AnnouncementStatus = AnnouncementStatus.NeverShown,
) : AnnouncementStore {
    private val spaceAnnouncement = MutableStateFlow(initialSpaceAnnouncementStatus)
    private val newNotificationSoundAnnouncement = MutableStateFlow(initialNewNotificationSoundAnnouncementStatus)

    override suspend fun setAnnouncementStatus(announcement: Announcement, status: AnnouncementStatus) {
        announcement.toMutableStateFlow().value = status
    }

    override fun announcementStatusFlow(announcement: Announcement): Flow<AnnouncementStatus> {
        return announcement.toMutableStateFlow().asStateFlow()
    }

    override suspend fun reset() {
        spaceAnnouncement.value = AnnouncementStatus.NeverShown
        newNotificationSoundAnnouncement.value = AnnouncementStatus.NeverShown
    }

    private fun Announcement.toMutableStateFlow() = when (this) {
        Announcement.Space -> spaceAnnouncement
        Announcement.NewNotificationSound -> newNotificationSoundAnnouncement
    }
}

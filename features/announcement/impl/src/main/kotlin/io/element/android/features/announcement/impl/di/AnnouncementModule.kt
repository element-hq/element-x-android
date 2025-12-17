/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.announcement.impl.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import io.element.android.features.announcement.impl.AnnouncementPresenter
import io.element.android.features.announcement.impl.AnnouncementState
import io.element.android.features.announcement.impl.spaces.SpaceAnnouncementPresenter
import io.element.android.features.announcement.impl.spaces.SpaceAnnouncementState
import io.element.android.libraries.architecture.Presenter

@ContributesTo(AppScope::class)
@BindingContainer
interface AnnouncementModule {
    @Binds
    fun bindAnnouncementPresenter(presenter: AnnouncementPresenter): Presenter<AnnouncementState>

    @Binds
    fun bindSpaceAnnouncementPresenter(presenter: SpaceAnnouncementPresenter): Presenter<SpaceAnnouncementState>
}

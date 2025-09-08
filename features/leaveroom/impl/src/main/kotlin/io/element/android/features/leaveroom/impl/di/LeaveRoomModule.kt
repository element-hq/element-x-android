/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.leaveroom.impl.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import io.element.android.features.leaveroom.api.LeaveRoomState
import io.element.android.features.leaveroom.impl.LeaveRoomPresenter
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.SessionScope

@ContributesTo(SessionScope::class)
@BindingContainer
interface LeaveRoomModule {
    @Binds
    fun bindLeaveRoomPresenter(presenter: LeaveRoomPresenter): Presenter<LeaveRoomState>
}

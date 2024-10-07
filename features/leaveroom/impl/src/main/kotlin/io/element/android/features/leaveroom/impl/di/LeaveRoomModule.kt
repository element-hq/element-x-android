/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.leaveroom.impl.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import io.element.android.features.leaveroom.api.LeaveRoomState
import io.element.android.features.leaveroom.impl.LeaveRoomPresenter
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.SessionScope

@ContributesTo(SessionScope::class)
@Module
interface LeaveRoomModule {
    @Binds
    fun bindLeaveRoomPresenter(presenter: LeaveRoomPresenter): Presenter<LeaveRoomState>
}

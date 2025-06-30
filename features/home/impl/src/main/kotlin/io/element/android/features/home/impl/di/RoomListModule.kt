/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import io.element.android.features.home.impl.filters.RoomListFiltersPresenter
import io.element.android.features.home.impl.filters.RoomListFiltersState
import io.element.android.features.home.impl.roomlist.RoomListPresenter
import io.element.android.features.home.impl.roomlist.RoomListState
import io.element.android.features.home.impl.search.RoomListSearchPresenter
import io.element.android.features.home.impl.search.RoomListSearchState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.SessionScope

@ContributesTo(SessionScope::class)
@Module
interface RoomListModule {
    @Binds
    fun bindRoomListPresenter(presenter: RoomListPresenter): Presenter<RoomListState>

    @Binds
    fun bindSearchPresenter(presenter: RoomListSearchPresenter): Presenter<RoomListSearchState>

    @Binds
    fun bindFiltersPresenter(presenter: RoomListFiltersPresenter): Presenter<RoomListFiltersState>
}

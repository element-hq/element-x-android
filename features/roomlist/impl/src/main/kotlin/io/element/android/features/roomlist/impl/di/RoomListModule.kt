/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomlist.impl.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import io.element.android.features.roomlist.impl.filters.RoomListFiltersPresenter
import io.element.android.features.roomlist.impl.filters.RoomListFiltersState
import io.element.android.features.roomlist.impl.search.RoomListSearchPresenter
import io.element.android.features.roomlist.impl.search.RoomListSearchState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.SessionScope

@ContributesTo(SessionScope::class)
@Module
interface RoomListModule {
    @Binds
    fun bindSearchPresenter(presenter: RoomListSearchPresenter): Presenter<RoomListSearchState>

    @Binds
    fun bindFiltersPresenter(presenter: RoomListFiltersPresenter): Presenter<RoomListFiltersState>
}

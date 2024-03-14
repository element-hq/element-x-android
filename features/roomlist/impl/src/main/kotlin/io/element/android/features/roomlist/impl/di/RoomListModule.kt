/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.roomlist.impl.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import io.element.android.features.roomlist.impl.filters.RoomListFiltersPresenter
import io.element.android.features.roomlist.impl.filters.RoomListFiltersState
import io.element.android.features.roomlist.impl.migration.MigrationScreenPresenter
import io.element.android.features.roomlist.impl.migration.MigrationScreenState
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

    @Binds
    fun bindMigrationScreenPresenter(presenter: MigrationScreenPresenter): Presenter<MigrationScreenState>
}

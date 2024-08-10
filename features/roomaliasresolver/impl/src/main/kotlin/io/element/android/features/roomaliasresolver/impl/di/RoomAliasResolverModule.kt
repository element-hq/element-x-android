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

package io.element.android.features.roomaliasresolver.impl.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import io.element.android.features.roomaliasresolver.impl.RoomAliasResolverPresenter
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomAlias

@Module
@ContributesTo(SessionScope::class)
object RoomAliasResolverModule {
    @Provides
    fun providesJoinRoomPresenterFactory(
        client: MatrixClient,
    ): RoomAliasResolverPresenter.Factory {
        return object : RoomAliasResolverPresenter.Factory {
            override fun create(roomAlias: String): RoomAliasResolverPresenter {
                return RoomAliasResolverPresenter(
                    roomAlias = roomAlias,
                    matrixClient = client,
                )
            }
        }
    }
}

/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.x.di

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.appnav.di.RoomComponentFactory
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.room.MatrixRoom
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class DefaultRoomComponentFactory @Inject constructor(
    private val roomComponentBuilder: RoomComponent.Builder
) : RoomComponentFactory {
    override fun create(room: MatrixRoom): Any {
        return roomComponentBuilder.room(room).build()
    }
}

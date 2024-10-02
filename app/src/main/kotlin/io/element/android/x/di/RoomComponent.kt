/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.x.di

import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.MergeSubcomponent
import dagger.BindsInstance
import io.element.android.libraries.architecture.NodeFactoriesBindings
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.room.MatrixRoom

@SingleIn(RoomScope::class)
@MergeSubcomponent(RoomScope::class)
interface RoomComponent : NodeFactoriesBindings {
    @MergeSubcomponent.Builder
    interface Builder {
        @BindsInstance
        fun room(room: MatrixRoom): Builder
        fun build(): RoomComponent
    }

    @ContributesTo(SessionScope::class)
    interface ParentBindings {
        fun roomComponentBuilder(): Builder
    }
}

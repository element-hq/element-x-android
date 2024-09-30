/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.impl.di

import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.MergeSubcomponent
import dagger.Subcomponent
import io.element.android.libraries.architecture.NodeFactoriesBindings
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.SingleIn

@SingleIn(CreateRoomScope::class)
@MergeSubcomponent(CreateRoomScope::class)
interface CreateRoomComponent : NodeFactoriesBindings {
    @MergeSubcomponent.Builder
    interface Builder {
        fun build(): CreateRoomComponent
    }

    @ContributesTo(SessionScope::class)
    interface ParentBindings {
        fun createRoomComponentBuilder(): Builder
    }
}

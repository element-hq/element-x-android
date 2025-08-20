/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import io.element.android.libraries.architecture.NodeFactoriesBindings
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient

@GraphExtension(SessionScope::class)
interface SessionComponent : NodeFactoriesBindings {
    val roomComponentFactory: RoomComponent.Factory

    @ContributesTo(AppScope::class)
    @GraphExtension.Factory
    interface Factory {
        fun createSessionComponent(@Provides matrixClient: MatrixClient): SessionComponent
    }
}

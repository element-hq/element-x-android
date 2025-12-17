/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.di

import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import io.element.android.libraries.architecture.NodeFactoriesBindings
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient

@GraphExtension(SessionScope::class)
interface SessionGraph : NodeFactoriesBindings {
    val roomGraphFactory: RoomGraph.Factory

    @GraphExtension.Factory
    interface Factory {
        fun create(@Provides matrixClient: MatrixClient): SessionGraph
    }
}

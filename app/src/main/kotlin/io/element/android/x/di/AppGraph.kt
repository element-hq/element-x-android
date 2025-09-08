/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.di

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import io.element.android.libraries.architecture.NodeFactoriesBindings
import io.element.android.libraries.di.annotations.ApplicationContext

@DependencyGraph(AppScope::class)
interface AppGraph : NodeFactoriesBindings {
    val sessionGraphFactory: SessionGraph.Factory

    @DependencyGraph.Factory
    interface Factory {
        fun create(
            @ApplicationContext @Provides
            context: Context
        ): AppGraph
    }
}

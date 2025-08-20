/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.di

import android.content.Context
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import io.element.android.libraries.architecture.NodeFactoriesBindings
import dev.zacsweers.metro.AppScope
import io.element.android.libraries.di.annotations.ApplicationContext

@GraphExtension(AppScope::class)
interface AppComponent : NodeFactoriesBindings {
    val sessionComponentFactory: SessionComponent.Factory

    @ContributesTo(GlobalScope::class)
    @GraphExtension.Factory
    interface Factory {
        fun create(
            @ApplicationContext @Provides
            context: Context
        ): AppComponent
    }
}

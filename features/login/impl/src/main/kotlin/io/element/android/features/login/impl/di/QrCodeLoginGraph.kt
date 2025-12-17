/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import io.element.android.libraries.architecture.NodeFactoriesBindings

@GraphExtension(QrCodeLoginScope::class)
interface QrCodeLoginGraph : NodeFactoriesBindings {
    @ContributesTo(AppScope::class)
    @GraphExtension.Factory
    interface Factory {
        fun create(): QrCodeLoginGraph
    }
}

/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.di

import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import io.element.android.libraries.architecture.NodeFactoriesBindings
import io.element.android.libraries.di.AppScope
import dev.zacsweers.metro.SingleIn

@SingleIn(QrCodeLoginScope::class)
@GraphExtension(QrCodeLoginScope::class)
interface QrCodeLoginComponent : NodeFactoriesBindings {
    @GraphExtension.Factory
    interface Factory {
        fun create(): QrCodeLoginComponent
    }
}

/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.di

import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.MergeSubcomponent
import io.element.android.libraries.architecture.NodeFactoriesBindings
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn

@SingleIn(QrCodeLoginScope::class)
@MergeSubcomponent(QrCodeLoginScope::class)
interface QrCodeLoginComponent : NodeFactoriesBindings {
    @MergeSubcomponent.Builder
    interface Builder {
        fun build(): QrCodeLoginComponent
    }

    @ContributesTo(AppScope::class)
    interface ParentBindings {
        fun qrCodeLoginComponentBuilder(): Builder
    }
}

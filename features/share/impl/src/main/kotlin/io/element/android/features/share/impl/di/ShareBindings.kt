/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.impl.di

import dev.zacsweers.metro.ContributesTo
import io.element.android.features.share.impl.SharePresenter
import io.element.android.libraries.di.SessionScope

/**
 * Used to get a [SharePresenter.Factory] from the graph of a session which is not the current one.
 */
@ContributesTo(SessionScope::class)
interface ShareBindings {
    fun sharePresenterFactory(): SharePresenter.Factory
}

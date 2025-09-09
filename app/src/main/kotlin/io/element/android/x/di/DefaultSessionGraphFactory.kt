/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.appnav.di.SessionGraphFactory
import io.element.android.libraries.matrix.api.MatrixClient

@ContributesBinding(AppScope::class)
@Inject class DefaultSessionGraphFactory(
    private val appGraph: AppGraph
) : SessionGraphFactory {
    override fun create(client: MatrixClient): Any {
        return appGraph.sessionGraphFactory.create(client)
    }
}

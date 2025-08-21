/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.di

import dev.zacsweers.metro.ContributesBinding
import io.element.android.appnav.di.SessionComponentFactory
import dev.zacsweers.metro.AppScope
import io.element.android.libraries.matrix.api.MatrixClient
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.asContribution

@ContributesBinding(AppScope::class)
@Inject
class DefaultSessionComponentFactory(
    private val appComponent: AppComponent
) : SessionComponentFactory {
    override fun create(client: MatrixClient): Any {
//        val asContribution = appComponent.sessionComponentFactory
//        return asContribution.createSessionComponent(client)
        return appComponent.sessionComponentFactory.createSessionComponent(client)
    }
}
